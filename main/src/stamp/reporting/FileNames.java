package stamp.reporting;

import soot.SootClass;
import shord.program.Program;
import stamp.srcmap.SourceInfo;

public class FileNames extends XMLReport
{
    public FileNames()
	{
		super("FileNames");
    }

    public void generate()
	{
        Program program = Program.g();
        for(SootClass c : program.getClasses())
		{
			newTuple()
				.setAttr("chordsig", c.getName())
				.setAttr("srcFile", SourceInfo.filePath(c))
				.setAttr("lineNum", String.valueOf(SourceInfo.classLineNum(c)));
		}
    }
}
