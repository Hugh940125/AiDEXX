#include "ctimer.h"
#include <future>

CTimer::CTimer():m_bExpired(true), m_bTryExpired(false), m_bLoop(false) {}

CTimer::~CTimer()
{
    m_bTryExpired = true;   //尝试使任务过期
    DeleteThread();
}

bool CTimer::Start(unsigned int msTime, std::function<void()> task, bool bLoop)
{
    if (!m_bExpired || m_bTryExpired) return false;  //任务未过期(即内部仍在存在或正在运行任务)
    m_bExpired = false;
    m_bLoop = bLoop;
    m_nCount = 0;
    
    DeleteThread();
    m_Thread = new std::thread([this, msTime, task]() {
        while (!m_bTryExpired) {
            m_ThreadCon.wait_for(m_ThreadLock, std::chrono::milliseconds(msTime));  //休眠
            if (!m_bTryExpired) {
                task();     //执行任务
                m_nCount ++;
                if (!m_bLoop) {
                    break;
                }
            }
        }
        m_bExpired = true;      //任务执行完成(表示已有任务已过期)
        m_bTryExpired = false;  //为了下次再次装载任务
    });

    return true;
}


void CTimer::Cancel()
{
    if (m_bExpired || m_bTryExpired || !m_Thread) {
        return;
    }
    m_bTryExpired = true;
    DeleteThread();
}

void CTimer::CancelLoop()
{
    m_bLoop = false;
}

void CTimer::DeleteThread()
{
    if (m_Thread) {
        m_ThreadCon.notify_all();   //休眠唤醒
        m_Thread->join();           //等待线程退出
        delete m_Thread;
        m_Thread = nullptr;
    }
}

