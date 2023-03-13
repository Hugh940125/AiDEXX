#ifndef CGMHISTORIESPARSER_H
#define CGMHISTORIESPARSER_H

#include "../../devcomm/CLibrary/global.h"
#include "cgmhistoryparser.h"

class CgmHistoriesParser : public CgmHistoryParser
{
public:
    static const int MIN_BYTES_LENGTH = 7;
    
    CgmHistoriesParser(const char *bytes, uint16 length) : CgmHistoryParser(bytes, length) {
        if (length < MIN_BYTES_LENGTH + 2) LOGE("bytes length: %d", length);
        first = true;
        mSensorIndex = 0;
        mStartUp = 0;
    }

    bool hasNext();
    
    const CgmHistoryPlusEntity *getHistoryPlus();
    const CgmHistoryPlusEntity *getFullHistoryPlus();
    
    const CgmEncryptedHistoryEntity *getEncryptedHistory();
    const CgmEncryptedHistoryPlusEntity *getEncryptedHistoryPlus();

protected:
    CgmHistoryPlusEntity historyPlus;
    CgmEncryptedHistoryEntity encryptedHistory;
    CgmEncryptedHistoryPlusEntity encryptedHistoryPlus;
    
    virtual void parse() override;

private:
    bool first;
    uint8 mSensorIndex;
    uint64 mStartUp;
};

#endif // CGMHISTORIESPARSER_H
