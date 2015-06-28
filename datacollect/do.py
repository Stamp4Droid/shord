import hashlib
import os
import subprocess
import sys
import shutil

import prettyprint_infer_alias_data

ellaHome = "/home/saswat/ella"
stampHome = "/home/saswat/shord-modular"
outputDir = "output"

def appId(apk_file):
	appId = os.path.abspath(apk_file).replace('/','_');
	if len(appId) > 100:
            return hashlib.sha256(appId).hexdigest();
	else:
            return appId;

def runElla(apkFile):
    instrInfoPath = stampHome + "/stamp_output/" + appId(apkFile) + "/inferaliasmodel/instrinfo.txt" 
    command = ellaHome + "/ella.sh i " + apkFile + " -ella.iinfo " + instrInfoPath
    process = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    out, err = process.communicate()
    returncode = process.wait()
    if returncode != 0:
        print err
        sys.exit("Error running Ella")
    else:
        print out
                    

def runStamp(apkFile):
    command = stampHome + "/stamp analyze " + apkFile
    process = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    out, err = process.communicate()
    returncode = process.wait()
    if returncode != 0:
        print err
        sys.exit("Error running Stamp")
    else:
        print out    

def getLatestCoverageDatFile(apkFile):
    ellaOutDir = ellaHome + "/ella-out/" + appId(apkFile)
    coverageFiles = [ f for f in os.listdir(ellaOutDir) if os.path.isfile(os.path.join(ellaOutDir,f)) and f.startswith("coverage.dat.") ]
    latest = sorted(coverageFiles)[-1] # Assumes lexicographically sortable dates
    return os.path.join(ellaOutDir, latest)

def postProcess(apkFile):
    outDir = outputDir + "/" + appId(apkFile)
    if not os.path.exists(outDir):
        shutil.rmtree(outDir)
        os.makedirs(outDir)
    instrInfoPath = stampHome + "/stamp_output/" + appId(apkFile) + "/inferaliasmodel/instrinfo.txt" 
    methodsPath = stampHome + "/stamp_output/" + appId(apkFile) + "/inferaliasmodel/methods.txt" 
    shutil.copy(instrInfoPath, outDir)
    shutil.copy(methodsPath, outDir)
    shutil.copy(getLatestCoverageDatFile(apkFile), outDir)    
    ppprocessor = prettyprint_infer_alias_data.PrettyPrintInferAliasProcessor(outDir)
    ppprocessor.process(outDir + "/data.txt")

if __name__ == "__main__":
    command = sys.argv[1]
    apkFile = sys.argv[2]
    if command == "i":
        runStamp(apkFile)
        runElla(apkFile)
    elif command == "p":
        postProcess(apkFile)
