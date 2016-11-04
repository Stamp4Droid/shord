echo java -jar -Dchord.work.dir=../pjbench/dacapo/benchmarks/$1 -Dchord.model.path=$2 shord.jar
java -jar -Dchord.work.dir=../pjbench/dacapo/benchmarks/$1 -Dchord.model.path=$2 shord.jar
echo mkdir -p results
mkdir -p results/$1/$3
echo mv ../pjbench/dacapo/benchmarks/$1/chord_output/*.txt results/$1/$3
mv ../pjbench/dacapo/benchmarks/$1/chord_output/*.txt results/$1/$3
