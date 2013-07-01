<%@ page import="stamp.reporting.FileManager"%>
<%
	String filepath = request.getParameter("filepath");
	boolean isModel = request.getParameter("isModel").equals("true");
    String lineNum = request.getParameter("lineNum");

	FileManager manager = (FileManager) session.getAttribute("manager");
	if(manager == null){
		String rootPath = (String)session.getAttribute("rootPath");
		String srcPath = (String)session.getAttribute("srcPath");
		String outPath = (String)session.getAttribute("outPath");
		String libPath = (String)session.getAttribute("libPath");
		manager = new FileManager(rootPath, outPath, libPath, srcPath);
		session.setAttribute("manager", manager);
	}
	
	String program = manager.getAnnotatedSource(filepath, isModel);
%><%=program%>
