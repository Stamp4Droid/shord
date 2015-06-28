import re
import os
import sys

METHODS_FILE_NAME = "methods.txt"
INST_INFO_FILE_NAME = "instrinfo.txt"

methodfile_line_re = re.compile(r"^(?P<m_id>[1-9][0-9]*) (?P<m_name><.*>)$")

def get_latest_coverage_dat_file(ella_run_dir):
	coverage_dat_files = [ f for f in os.listdir(ella_run_dir) if os.path.isfile(os.path.join(ella_run_dir,f)) and f.startswith("coverage.dat") ]
	latest = sorted(coverage_dat_files)[-1] # Assumes lexicographically sortable dates
	return os.path.join(ella_run_dir,latest)

class MethodCallArgumentRecord:

	def __init__(self, event_id, method, offset, arg):
		self.type = "METHCALLARG"
		self.id = event_id
		self.method = method
		self.offset = offset
		self.arg = arg
		
	def to_string_with_value(self,value):
		if int(self.arg) == 0:
			return str(self.id) + "\t" + "NEW" + "\t" + self.method + ":" + self.offset + "\t" + value
		else:
			return str(self.id) + "\t" + self.type + "\t" + self.method + ":" + self.offset + "\t" + self.arg + "\t" + value

class MethodParameterRecord:
	
	def __init__(self, event_id, method, arg):
		self.type = "METHPARAM"
		self.id = event_id
		self.method = method
		self.arg = arg
		
	def to_string_with_value(self,value):
		return str(self.id) + "\t" + self.type + "\t" + self.method + "\t" + self.arg + "\t" + value

class PrettyPrintInferAliasProcessor:

	def __init__(self,input_directory):
		self.in_dir = input_directory
		self.methods_map = {}
		self.events_map = {}
	
	def process_methods_file(self):
		with open(os.path.join(self.in_dir,METHODS_FILE_NAME),"r") as f:
			for line in f:
				line = line.strip()
				method_entry_match = methodfile_line_re.match(line)
				if not method_entry_match:
					raise Exception("Invalid line in methods file: " + line)
				method_id = int(method_entry_match.group("m_id"))
				method_name = method_entry_match.group("m_name")
				if method_id in self.methods_map.keys():
					raise Exception("Non unique method id detected: " + method_id + " (line: " + line + ")")
				self.methods_map[method_id] = method_name
	
	def process_instrumentation_info_file(self):
		with open(os.path.join(self.in_dir,INST_INFO_FILE_NAME),"r") as f:
			for line in f:
				line = line.strip()
				parts = line.split()
				if parts[0] == "METHCALLARG":
					#METHCALLARG <init>(Landroid/content/Context;Landroid/util/AttributeSet;)V@Lcom/eat24/app/widgets/Eat24Button; 5 0 0 3870 4
					record_id = int(parts[4])
					method_id = int(parts[5])
					if method_id not in self.methods_map.keys():
						raise Exception("Unknown method id in instrumentation info file: " + method_id + " (line: " + line + ")")
					method = self.methods_map[method_id]
					record = MethodCallArgumentRecord(record_id,method,parts[6],parts[3])
				elif parts[0] == "METHPARAM":
					#METHPARAM setEnabled(Z)V@Lcom/eat24/app/widgets/Eat24Button; 0 6 3873
					record_id = int(parts[3])
					method_id = int(parts[4])
					if method_id not in self.methods_map.keys():
						raise Exception("Unknown method id in instrumentation info file: " + method_id + " (line: " + line + ")")
					method = self.methods_map[method_id]
					record = MethodParameterRecord(record_id,method,parts[2])
				else:
					raise Exception("Invalid line in instrumentation info file (unknown instruction type): " + line)
				
				if record_id in self.events_map.keys():
					raise Exception("Non unique record id detected: " + record_id + " (line: " + line + ")")
				self.events_map[record_id] = record
	
	def process_coverage_log(self,outfile):
		if outfile != None:
			fout = open(outfile, 'w')
			
		with open(get_latest_coverage_dat_file(self.in_dir),"r") as f:
			in_header = True
			for line in f:
				line = line.strip()
				if "###" in line:
					in_header = False
					continue
				if in_header:
					continue
				parts = line.split()
				assert len(parts) == 2
				record_id = int(parts[0])
				value = parts[1]
				if record_id not in self.events_map.keys():
					raise Exception("Unknown record id in coverage.dat file: " + record_id + " (line: " + line + ")")
				record = self.events_map[record_id]
				if outfile == None:
					print record.to_string_with_value(value)
				else:
					fout.write(record.to_string_with_value(value) + "\n")
		if outfile != None:
			fout.close()
		
	def process(self,outfile=None):
		self.process_methods_file()
		self.process_instrumentation_info_file()
		self.process_coverage_log(outfile)
		pass

def main():
	assert len(sys.argv) == 2
	ppprocessor = PrettyPrintInferAliasProcessor(sys.argv[1])
	ppprocessor.process()

if __name__ == "__main__":
    main()
