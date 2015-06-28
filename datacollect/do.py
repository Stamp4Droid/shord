import hashlib
import os
import subprocess
import sys
import shutil

import prettyprint_infer_alias_data

ellaHome = "/home/saswat/ella"
stampHome = "/home/saswat/shord-modular"
outputDir = "output"
buildToolDir = "/home/saswat/software/android-sdk-linux/build-tools/20.0.0/"

def getPackageName(apkFile):
    command = buildToolDir + "/aapt dump badging " + apkFile
    process = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    out, err = process.communicate()
    returncode = process.wait()
    if returncode != 0:
        print err
        sys.exit("Error running aapt")
    else:
        for line in out.splitlines():
            if line.startswith("package: name='"):
                i = len("package: name='")
                j = line.index("'", i)
                return line[i:j]

def runCommand(command):
    print command
    process = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    out, err = process.communicate()
    returnCode = process.wait()
    if returnCode != 0:
        print err
    else:
        print out
    return returnCode

def appId(apk_file):
	appId = os.path.abspath(apk_file).replace('/','_');
	if len(appId) > 100:
            return hashlib.sha256(appId).hexdigest();
	else:
            return appId;

def uninstall(pkgName):
    command = "adb uninstall "+pkgName
    return runCommand(command)

def install(apkFile):
    command = "adb install "+apkFile
    return runCommand(command)

def runElla(apkFile):
    instrInfoPath = stampHome + "/stamp_output/" + appId(apkFile) + "/inferaliasmodel/instrinfo.txt" 
    command = ellaHome + "/ella.sh i " + apkFile + " -ella.iinfo " + instrInfoPath
    return runCommand(command)

def runStamp(apkFile):
    command = stampHome + "/stamp analyze " + apkFile
    return runCommand(command)

def getLatestCoverageDatFile(apkFile):
    ellaOutDir = ellaHome + "/ella-out/" + appId(apkFile)
    coverageFiles = [ f for f in os.listdir(ellaOutDir) if os.path.isfile(os.path.join(ellaOutDir,f)) and f.startswith("coverage.dat.") ]
    latest = sorted(coverageFiles)[-1] # Assumes lexicographically sortable dates
    return os.path.join(ellaOutDir, latest)

def postProcess(apkFile):
    if not os.path.exists(outputDir):
        os.makedirs(outputDir)
    outDir = outputDir + "/" + appId(apkFile)
    print outDir
    if os.path.exists(outDir):
        shutil.rmtree(outDir)
    os.mkdir(outDir)
    instrInfoPath = stampHome + "/stamp_output/" + appId(apkFile) + "/inferaliasmodel/instrinfo.txt" 
    methodsPath = stampHome + "/stamp_output/" + appId(apkFile) + "/inferaliasmodel/methods.txt" 
    shutil.copy(instrInfoPath, outDir)
    shutil.copy(methodsPath, outDir)
    shutil.copy(getLatestCoverageDatFile(apkFile), outDir)    
    ppprocessor = prettyprint_infer_alias_data.PrettyPrintInferAliasProcessor(outDir)
    ppprocessor.process(outDir + "/data.txt")
    print "Coverage data stored in "+os.path.abspath(outDir + "/data.txt")

if __name__ == "__main__":
    command = sys.argv[1]
    apkFile = sys.argv[2]

    if command == "i":
        if runStamp(apkFile) != 0 :
		sys.exit("Error running Stamp")
        if runElla(apkFile) != 0 :
		sys.exit("Error running Ella")

    elif command == "s":
        pkgName = getPackageName(apkFile)
        print "package: "+pkgName

        #uninstall
        if uninstall(pkgName) != 0:
            print "Error while uninstalling"

        #install                                                                                                                                                                                                                             
        if install(apkFile) != 0:
            print "Error while installing"
            sys.exit(1)

    elif command == "d":
        if runCommand("adb shell am broadcast -a com.apposcopy.ella.COVERAGE --es action \"e\"") != 0 :
            print "Erro while broadcasting"
            sys.exit(1)

        #uninstall                                                                                                                                                                                                                           
        pkgName = getPackageName(apkFile)
        if uninstall(pkgName) != 0:
            print "Error while uninstalling"

    elif command == "p":
        postProcess(apkFile)

    else:
        print "Expect one of [i,s,d,p] as the first argument. i = instrument, s = install app, d = finish recording, p = post-processing."
     
