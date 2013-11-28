"""
Step 1: For each app, run featureList.sql to get F1 to F10;
Step 2: Calculate the type of component that has more than 3 flows;
Step 3: Output the source-sink in step2.
"""
import sqlite3 as lite
from subprocess import PIPE, Popen
import sys
import os
import datetime


def checkFeature(dbDir, sqlDir, appDir):
    #open the feature sql.
    queryFeature = ''
    with open (sqlDir, "r") as myfile:
        queryFeature = myfile.read().replace('\n', '')

    myTable = {
        'DroidDream' : [False, False, False, False, False, False, False, False, False, False]
        ,
        'DroidDreamLight' : [False, False, False, False, False, False, False, False, False, False]

        ,
        'GoldDream' : [False, False, False, False, False, False, False, False, False, False]

        ,
        'Geinimi' : [False, False, False, False, False, False, False, False, False, False]

        ,
        'Pjapps' : [False, False, False, False, False, False, False, False, False, False]

        ,
        'DroidKungFu1' : [False, False, False, False, False, False, False, False, False, False]

        ,
        'DroidKungFu2' : [False, False, False, False, False, False, False, False, False, False]

        ,
        'DroidKungFu3' : [False, False, False, False, False, False, False, False, False, False]

        ,
        'DroidKungFu4' : [False, False, False, False, False, False, False, False, False, False]

        ,
        'BaseBridge' : [False, False, False, False, False, False, False, False, False, False]

        ,
        'ADRD' : [False, False, False, False, False, False, False, False, False, False]
        ,
        'BeanBot' : [False, False, False, False, False, False, False, False, False, False]
        ,
        'Bgserv' : [False, False, False, False, False, False, False, False, False, False]
        ,
        'AnserverBot' : [False, False, False, False, False, False, False, False, False, False]
        ,
        'CoinPirate' : [False, False, False, False, False, False, False, False, False, False]
        ,
        'DroidCoupon' : [False, False, False, False, False, False, False, False, False, False]
        ,
        'jSMSHider' : [False, False, False, False, False, False, False, False, False, False]
        ,
        'GingerMaster' : [False, False, False, False, False, False, False, False, False, False]

    }


    try:

        con = lite.connect(dbDir)
        
        cur = con.cursor()    
        cur.execute('SELECT * from iccg')
        
        rows = cur.fetchall()

        for row in rows:
        #step1
            apkId = str(row[0])
            apkName = row[1] 

            #replace the actual iccg_id.
            currentQuery = queryFeature.replace('?', apkId)
            #print '***************' + key
            #print currentQuery

            cur.execute(currentQuery)
            features = cur.fetchone()

            grep = "find " + appDir + " -iname " + apkName
            output, error = Popen(
                grep.split(" "), stdout=PIPE, stderr=PIPE).communicate()

            nodeId = str(features[11])
            queryFlow = "SELECT source, sink FROM flow where src_node_id="+nodeId+"""
               and flow.src_node_id=flow.sink_node_id and 
               ( flow.sink='!INTERNET' or flow.sink='!File' or flow.sink='!FILE' or 
                 flow.sink='!EXEC' or flow.sink='!WebView' or flow.sink='!ENC/DEC' or flow.sink='!SOCKET' ) and 
               (source='$getDeviceId' or source='$getLine1Number' or source='$getSubscriberId' or
                source='$getSimSerialNumber' or source='$SDK' or source='$MODEL' or source='$BRAND' or
                source='$File' or source='$ENC/DEC' or source='$InstalledPackages' or
                source='$content://sms' or source='$RELEASE' or source='$PRODUCT' or
                source='MANUFACTURER')"""

        
            output =  output.replace('\n', '') 
            familyName = output.split('/')[3]

            if not myTable.has_key(familyName):
                continue
                #myTable[familyName] = [False, False, False, False, False, False, False, False, False, False]

            for i in range(10):
                myTable[familyName][i] = (myTable[familyName][i] or (features[i]>0))

            if nodeId <> 'None':
                cur.execute(queryFlow)
                flows = cur.fetchall()

            #print output + ": " + str(features[0]>0) + ' ' + str(features[1]>0) + ' ' + str(features[2]>0) + ' ' + str(features[3]>0) +' ' + str(features[4]>0) + ' '+str(features[5]>0) +' ' + str(features[6]>0) + ' ' + str(features[7]>0) + ' '+  str(features[8]>0) + ' '+ str(features[9]>0) + ' ' + str(features[10]) + ' ' + str(features[11])

    except lite.Error, e:
        
        print "Error %s:" % e.args[0]
        sys.exit(1)
        
    finally:
        for key in myTable:
            print key
            print myTable[key]

        for key in myTable:
            for key2 in myTable:
                if key == key2:
                    continue
                if myTable[key2] == myTable[key]:
                    print key + '======'+ key2










        
        if con:
            con.close()




def main():
    if len(sys.argv) < 4:
        print "Invalid arguments, you must provide app, db and sql."
        return
      
    dbDir = sys.argv[1]
    sqlDir = sys.argv[2]
    appDir = sys.argv[3]
    
    starttime = datetime.datetime.now()

    checkFeature(dbDir, sqlDir,appDir)

    endtime = datetime.datetime.now()
    print "Total execute time:"
    print (endtime - starttime)

if __name__ == "__main__":
        main()

