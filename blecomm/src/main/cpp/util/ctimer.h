#ifndef CTIMER_H
#define CTIMER_H

#include <stdio.h>
#include <functional>
#include <chrono>
#include <thread>
#include <atomic>
#include <mutex>
#include <string>
#include <condition_variable>

class CTimer
{
public:
    CTimer();
    ~CTimer();

    /**
     开始运行定时器
     @param msTime 延迟运行(单位ms)
     @param task 任务函数接口
     @param bLoop 是否循环(默认执行1次)
     @return true:已准备执行，否则失败
     */
    bool Start(unsigned int msTime, std::function<void()> task, bool bLoop = false);
    
    
    /**
     取消定时器
     */
    void Cancel();

    /**
     取消循环
     */
    void CancelLoop();


    /**
     异步执行一次任务
     @param msTime 延迟及间隔时间
     @param fun 函数接口或lambda代码块
     @param args 参数
     @return true:已准备执行，否则失败
     */
    template<typename callable, typename... arguments>
    bool AsyncOnce(int msTime, callable&& fun, arguments&&... args) {
        std::function<typename std::result_of<callable(arguments...)>::type()> task(std::bind(std::forward<callable>(fun), std::forward<arguments>(args)...));

        return Start(msTime, task, false);
    }


    /**
     异步循环执行任务
     @param msTime 延迟及间隔时间
     @param fun 函数接口或lambda代码块
     @param args 参数
     @return true:已准备执行，否则失败
     */
    template<typename callable, typename... arguments>
    bool AsyncLoop(int msTime, callable&& fun, arguments&&... args) {
        std::function<typename std::result_of<callable(arguments...)>::type()> task(std::bind(std::forward<callable>(fun), std::forward<arguments>(args)...));

        return Start(msTime, task, true);
    }

private:
    void DeleteThread();    //删除任务线程

public:
    int m_nCount = 0;   //循环次数

private:
    std::atomic_bool m_bExpired;       //装载的任务是否已经过期
    std::atomic_bool m_bTryExpired;    //装备让已装载的任务过期(标记)
    std::atomic_bool m_bLoop;          //是否循环

    std::thread *m_Thread = nullptr;
    std::mutex m_ThreadLock;
    std::condition_variable_any m_ThreadCon;
};


#endif // CTIMER_H
