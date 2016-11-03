echo java -jar -Dchord.work.dir=../pjbench/dacapo/benchmarks/$3 -Dchord.model.path=$1 shord.jar
java -jar -Dchord.work.dir=../pjbench/dacapo/benchmarks/$3 -Dchord.model.path=$1 shord.jar
echo mkdir -p results
mkdir -p results
echo mv ../pjbench/dacapo/benchmarks/chord_output/log.txt results/log_$3_$2.txt
mv ../pjbench/dacapo/benchmarks/chord_output/log.txt results/log_$3_$2.txt