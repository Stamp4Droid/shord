<%@ page import="stamp.reporting.ClassIndexGenerator"%>
<%
	String rootPath = (String) session.getAttribute("rootPath");
	String srcPath = (String) session.getAttribute("srcPath");
	
	
	ClassIndexGenerator cig = (ClassIndexGenerator) session.getAttribute("cig");
    if(cig == null){
    	cig = new ClassIndexGenerator(srcPath, rootPath);
    	session.setAttribute("cig", cig);
    }

	String type = request.getParameter("type");
	String pkgName = request.getParameter("pkgName");
	String index = cig.generate(type, pkgName);
	out.println(index);
%>
 