<%@ page import="stamp.reporting.FileManager"%>
<%
	String chordSig = request.getParameter("chordsig");

	FileManager manager = (FileManager) session.getAttribute("manager");
	if(manager == null){
		String rootPath = (String)session.getAttribute("rootPath");
		String srcPath = (String)session.getAttribute("srcPath");
		String outPath = (String)session.getAttribute("outPath");
		String libPath = (String)session.getAttribute("libPath");
		manager = new FileManager(rootPath, outPath, libPath, srcPath);
		session.setAttribute("manager", manager);
	}
	
	String classInfo = manager.getClassInfo(chordSig);
%><%=classInfo%>
