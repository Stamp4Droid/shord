import hashlib
import os
import subprocess
import sys
import shutil

import prettyprint_infer_alias_data


def init():
    settingsFile = os.path.dirname(os.path.abspath(__file__))+"/.settings"
    if not os.path.exists(settingsFile):
        print "Please copy wrench.settings.template into wrench.settings and " + \
            "set the configuration parameters in that file."
        exit(1)
    else:
        global stampHome, ellaHome, buildToolDir, outputDir
        with open(settingsFile,'r') as f:
            for line in f:
                line = line.strip()
                if len(line) == 0 : continue
                if line.startswith("#"): continue
                parts = line.split("=")
                assert len(parts) == 2
                if parts[0].strip() == "stamp.dir":
                    stampHome = parts[1].strip()
                elif parts[0].strip() == "ella.dir":
                    ellaHome = parts[1].strip()
                elif parts[0].strip() == "output.dir":
                    outputDir = parts[1].strip()
                elif parts[0].strip() == "buildtool.dir":
                    buildToolDir = parts[1].strip()

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
    instrInfoPath = stampHome + "/stamp_output/" + appId(apkFile) + "/inferaliasmodel/instrinfo.txt" 
    methodsPath = stampHome + "/stamp_output/" + appId(apkFile) + "/inferaliasmodel/methods.txt" 
    shutil.copy(instrInfoPath, outputDir)
    shutil.copy(methodsPath, outputDir)
    coverageDatFile = getLatestCoverageDatFile(apkFile)
    shutil.copy(coverageDatFile, outputDir)    
    ppprocessor = prettyprint_infer_alias_data.PrettyPrintInferAliasProcessor(outputDir)
    pkgName = getPackageName(apkFile)
    ppprocessor.process(outputDir + "/"+pkgName+".log")
    shutil.copy(apkFile, outputDir+ "/"+pkgName+".apk")
    os.remove(outputDir+"/instrinfo.txt")
    os.remove(outputDir+"/methods.txt")
    os.remove(outputDir+"/"+os.path.basename(coverageDatFile))
    print "Coverage data stored in "+os.path.abspath(outputDir + "/"+pkgName+".log")

def installCommand(apkFile):
    pkgName = getPackageName(apkFile)
    print "package: "+pkgName
    
    #uninstall
    if uninstall(pkgName) != 0:
        print "Error while uninstalling"

    #install                                                           
    if install(apkFile) != 0:
        print "Error while installing"
        sys.exit(1)

def doneCommand(apkFile):
    if runCommand("adb shell am broadcast -a com.apposcopy.ella.COVERAGE --es action \"e\"") != 0 :
        print "Erro while broadcasting"
        sys.exit(1)
        
    #uninstall                                                       
    pkgName = getPackageName(apkFile)
    if uninstall(pkgName) != 0:
        print "Error while uninstalling"
    

if __name__ == "__main__":
    command = sys.argv[1]
    apkFile = sys.argv[2]
    init()

    if command == "i":
        if runStamp(apkFile) != 0 :
		sys.exit("Error running Stamp")
        if runElla(apkFile) != 0 :
		sys.exit("Error running Ella")

    elif command == "s":
        installCommand(apkFile)

    elif command == "d":
        doneCommand(apkFile)
    
    elif command == "sd":
        installCommand(apkFile)
        print "Input anything when done testing the app."
        sys.stdin.read(1); #wait
        doneCommand(apkFile)

    elif command == "p":
        postProcess(apkFile)

    else:
        print "Expect one of [i,s,d,p] as the first argument. i = instrument, s = install app, d = finish recording, p = post-processing."
     
