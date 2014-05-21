# DO THIS FIRST: emulator -wipe-data -avd DR_AVD_403 &
python profile.py apks/ --tracedir traces/ --traceoutdir traceouts/ --monkey-presses 10000
