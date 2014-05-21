#!/usr/bin/env python
import os
import subprocess
import argparse
import re
import time

def parseargs():
    parser = argparse.ArgumentParser(description="Automatically launches and profiles a set of apk files")
    parser.add_argument("apkdir", type=str, help="the directory containing the apks to profile")
    parser.add_argument("--tracedir", type=str, help="the output directory for trace files", default="./")
    parser.add_argument("--traceoutdir", type=str, help="the output directory for traceout files", default="./")
    parser.add_argument("--monkey-presses", type=int, help="the number of events for monkey to execute to excersize the app", default=20)
    args = parser.parse_args()
    return (args.apkdir, args.tracedir, args.traceoutdir, args.monkey_presses)

if __name__ == "__main__":
    (apkdir, tracedir, traceoutdir, monkey_presses) = parseargs()
    build_tools_dir = '/home/obastani/Documents/android-sdk-linux/build-tools/19.0.1/';

    apps = os.listdir(apkdir)
    print("Preparing to profile "+str(len(apps))+" apk files")

    for appname in apps:
        app = apkdir+appname
        print("Profiling "+app)

        # Get app package
        appManifest = subprocess.check_output(["java", "ReadApk", app]).split('\n')
        for line in appManifest:
            if 'package=' in line:
                manifestPackagename = re.search('package="([^"]*)"', line).group(1)
                break
        print 'package name: ' + manifestPackagename


        # Install App
        subprocess.call(["adb", "install", app])

        # Get App Package / Process name
        badging = subprocess.check_output([build_tools_dir+"aapt", "dump", "badging", app])#, "|", "grep", "package", "|", "sed", "s/package: name\='\([^']*\).*/\\1/"])
        m = re.search("package: name='([^']*)", badging)
        if m:
            packagename = m.group(1)
            print packagename
        else:
            print "No package found...?"

        # Get all package names to pass to monkey (hax)
        ddoutput = subprocess.check_output([build_tools_dir+"dexdump", "-l", "xml", app])
        ddoutputlist = re.findall('package name="([^"]+)',ddoutput)
        #ddoutputliststr = ' -p '.join(ddoutputlist[0:29])
        ddoutputliststr = manifestPackagename
        ddoutputliststr = ddoutputliststr.lstrip().rstrip()
        # TODO limit length of list
        
        # launch app with monkey
        print subprocess.list2cmdline(["adb", "shell", "monkey", "-p", ddoutputliststr, "-v", str(5)])
        subprocess.call(["adb", "shell", "monkey", "-p", ddoutputliststr, "-v", str(5)])
        #print psresult
        n = False
        count = 0
        while not n and count < 100:
            psresult = subprocess.check_output(["adb", "shell", "ps"])
            n = re.search("\n(.*)"+packagename, psresult)
            print count
            count = count + 1
        try:
            pid = filter(len,n.group(1).split(' '))[1]
        except AttributeError:
            continue
        print "pid is", pid

        # Start Profilining
        subprocess.call(["adb", "shell", "am", "profile", pid, "start", "/sdcard/"+appname+".trace"])

        # Generate inputs / activity
        subprocess.call(["adb", "shell", "monkey", "-p", packagename, "-p", ddoutputliststr, "-v", str(monkey_presses)])

        # Stop Profiling
        subprocess.call(["adb", "shell", "am", "profile", str(pid), "stop"])

        # Apparently log dump is delayed from profile, so wait here 
        # Otherwise we get an empty file
        print "Wait Here..."
        time.sleep(5)

        if not os.path.isdir(tracedir):
            os.makedirs(tracedir)

        # Retrieve trace from device
        val = subprocess.call(["adb", "pull", "/sdcard/"+appname+".trace", tracedir])
        subprocess.call(["adb", "shell", "kill", pid])

        if not os.path.isdir(traceoutdir):
            os.makedirs(traceoutdir)

        # Convert trace to sequence format
        os.system("dmtracedump -o "+tracedir+appname+".trace > "+traceoutdir+appname+".traceout")

        # Uninstall the app
        # adb shell pm uninstall packagename
        subprocess.call(["adb", "shell", "pm", "uninstall", manifestPackagename])
        
