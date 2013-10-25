<!DOCTYPE html>
<html lang="en" class="fuelux">
	<head>
	<%@ page import="java.io.*,java.util.*,stamp.reporting.QueryResults,stamp.droidrecordweb.DroidrecordProxyWeb"%>
  <%
	if(!session.isNew()){
		session.invalidate();
		session = request.getSession();
	}
  
  	String propertyFile = request.getParameter("propertyfile");
  	Properties props = new Properties();
  	try{
  		props.load(new FileInputStream(propertyFile));
  	}catch(IOException e){
  		out.println(e.getMessage());
  	}
  	
     String rootPath = props.getProperty("rootPath");
     String outPath = props.getProperty("outPath");
     String appPath = props.getProperty("appPath");
     String srcPath = props.getProperty("srcPath");
     String libPath = props.getProperty("libPath");
     String dr_log_template = props.getProperty("stamp.droidrecord.logfile.template");
     String dr_log_bin = props.getProperty("stamp.droidrecord.logfile.bin");
     DroidrecordProxyWeb droidrecord = new DroidrecordProxyWeb(dr_log_template, dr_log_bin);
     System.out.println("DEBUG: " + outPath);
     boolean useJimple = outPath.matches("^.*\\.apk$");
     System.out.println("DEBUG: boolean useJimple = " + useJimple);

     session.setAttribute("rootPath", rootPath);
     session.setAttribute("appPath", appPath);
     session.setAttribute("outPath", outPath);
     session.setAttribute("srcPath", srcPath);
     session.setAttribute("libPath", libPath);
     session.setAttribute("droidrecord", droidrecord);
     session.setAttribute("useJimple", useJimple);

     System.out.println("srcPath = "+srcPath);
     
     session.setAttribute("msg", new LinkedList<String>());

     QueryResults qr2 = (QueryResults)session.getAttribute("qr");
     if(qr2 == null) {
          qr2 = new QueryResults();
          session.setAttribute("qr", qr2);
     }

        int numFlows = 0;
	SortedMap<String,String> titleToFileName = new TreeMap();     
	try {
		File reportTxt = new File(outPath+"/reports.txt");
		BufferedReader reader = new BufferedReader(new FileReader(reportTxt));
		String line; 
		while((line = reader.readLine()) != null){
			int i = line.lastIndexOf(" ");
			String fileName = line.substring(i+1);
			String title = line.substring(0,i);
			titleToFileName.put(title, fileName);
                        if(title.equals("Source-to-sink Flows")) {
                                String results = qr2.querySrcSinkFlows(fileName);
                                numFlows = results.split(",").length;
                        }
		}
	} catch(IOException e) {
		out.println(e.getMessage());
	}

	int tabCount = numFlows + titleToFileName.size() + 4 /* app, model, framework, jimple */;
  %>	  	
		<link href="/stamp/fuelux/css/fuelux.min.css" rel="stylesheet" />
		
		<style type="text/css">
			body {
				padding-top: 60px;
				padding-bottom: 20px;
    		}
    		
     		.source-view {height:80vh; overflow:auto; resize:auto;}
     		.right-view {overflow:auto; }
     		
     		.fuelux .nav-tabs > li > a {
     			padding-top: 0px;
     			padding-bottom: 5px;
     			line-height: 10px;
     		}
     		
     		.linenums > li {
     			white-space:pre;
     		}
     		
     		.result-container { 
 				position: relative;
				padding: 10px 15px 0 15px;
				overflow-x: hidden;
				overflow-y: auto;
				border: 1px solid #BBBBBB;
				border-radius: 4px 4px 4px 4px;
			}
     		
     		.src-ln-covered {
                background-color: rgba(255,255,0,0.2);
            }
  		</style>
  		
		<link href="/stamp/fuelux/css/fuelux-responsive.min.css" rel="stylesheet" />
		<link href="/stamp/css/prettify.css" type="text/css" rel="stylesheet"/>  
	</head>

	<body>
		<%@include file="navbar.jsp"%>
		
		<div class="container-fluid">
			<div class="row-fluid">
 				<div class="span3">
					<!--Sidebar content-->
					<%@include file="leftbar.jsp" %>
				</div>
				<div class="span6" id="centerpane">
					<ul class="nav nav-tabs" id="codetabs">
					<!--span class="label label-info" id="filename">
					</span>
					<div class="source-view" id="codeview">
					</div-->
					</ul>
					<div class="tab-content" id="codetabcontents">
					</div>
				</div>
				<div class="span3 right-view" id="rightside">
					<div id="rightbar">
					</div>
				</div>
			</div>
		</div>
			
		<script src="/stamp/jquery/1.8.2/jquery.min.js" type="text/javascript"></script>
		<script src="/stamp/fuelux/loader.js" type="text/javascript"></script>
		<script src="/stamp/scripts/prettify.js" type="text/javascript"></script>
		<script src="/stamp/scripts/viewSource.js" type="text/javascript"></script>
		
		<script>
		$('#codetabs').hide();
		</script>
		
		<script>		
			var numFlows = <%=numFlows%>;
			function showTab(index){
				for(j = 0; j < <%=tabCount%>; j++){
					if(index == j){
						$("#leftbartab-"+j).show();
					} else {
						$("#leftbartab-"+j).hide();
					}
				}
			}
			showTab(-1);
			
			<%
			for(i = 0; i < tabCount; i++){
			%>
				$("#showtab-<%=i%>").click(function(e){
					e.preventDefault();
					showTab(<%=i%>);
				});
			<%
			}
			%>
		</script>
		
		<script>
		  function contract(b,id) {
		    document.getElementById(id).style.display = "none";
		    b.innerHTML = "Expand";
		    b.setAttribute("onclick", "expand(this,'"+id+"')");
		  }

		  function expand(b,id) {
		    document.getElementById(id).style.display = "";
		    b.innerHTML = "Contract";
		    b.setAttribute("onclick", "contract(this,'"+id+"');");
		  }
		  
		  function switchHTMLObject(b,html1,html2,obj1,obj2) {
		    document.getElementById(obj1).style.display = "none";
		    document.getElementById(obj2).style.display = "";
		    b.setAttribute("onclick", "switchHTMLObject(this,'"+html2+"','"+html1+"','"+obj2+"','"+obj1+"')");
		    b.innerHTML = html1;
		  }
		</script>


		
		<script>
			var ClassHierarchyDataSource = function(type){
					this.srcType = type;
			};

			ClassHierarchyDataSource.prototype = {
					data : function(options, callback) {
						$.ajax({
							type: "POST",
							url: "/stamp/html/classIndex.jsp",
							data: {type: this.srcType, pkgName: options.name}
						}).done(function (response) {
							callback({data: $.parseJSON(response)});
						});
					}
			};
			
		    $('#AppHierarchy').tree({dataSource: new ClassHierarchyDataSource("app")});
		    $('#ModelsHierarchy').tree({dataSource: new ClassHierarchyDataSource("model")});
		    $('#FrameworkHierarchy').tree({dataSource: new ClassHierarchyDataSource("framework")});
		    $('#JimpleHierarchy').tree({dataSource: new ClassHierarchyDataSource("jimple")});
		</script>
		
		<script>
			var filePathToId = new Object();
			var tabNameToId = new Object();
			var idToHighlightedLine = new Object();
			var totalFilesOpened = 0;
			var flowSwitches = [];
			for (var ii = 0; ii < numFlows; ++ii) {
				flowSwitches.push(true);
			}
			
			function rightBarAddDynamicData(drDataParams)
			{
			    var data = jQuery.parseJSON(atob(drDataParams));
			    if(data == null) return;
			    var html = ViewSource.droidrecordDataToTable(data, false);
			    $("#rightbar div.droidrecord-runtime-parameters").html(html);
			}

			function anyTaintedFlowShowing(taintedFlows) {
				for (var i = 0; i < taintedFlows.length; ++i) {
					if (flowSwitches[taintedFlows[i]-1]) {
						return true;
					}
				}
				return false;
			}

			function colorTaint(href) {
				href = href.replace('#','');
				var taintedVariables = $('#'+href).find("[name=taintedVariable]");
			    for(var i=0; i<taintedVariables.length; ++i) {
			    	var flowString = taintedVariables[i].getAttribute("flows");
			    	var taintedFlows = flowString.split(':');
			    	if (flowString === 'null' || anyTaintedFlowShowing(taintedFlows)) {
						taintedVariables[i].setAttribute("style", "background-color:#FFB2B2");
					} else if (taintedVariables[i].hasAttribute('style')) {
						taintedVariables[i].removeAttribute('style');
					}
			    }
			}

			function compactFlowCtxtTable($table) {
				var $tds = $table.find('td');

				$tds.each(function (index) {
						var tex = $(this).text();
						var a_regex = /.* (.+)\(.*\)$/;
						var match = tex.match(a_regex);
						$(this).html(match[1]);
					});
			}

			function showCode(response, href)
			{
				var $flowtable = $('#centerpane #flowctxttable');
				if ($flowtable.length > 0) {
					compactFlowCtxtTable($flowtable);
					$('#rightside').append($flowtable[0].outerHTML);
					$flowtable.remove();
					registerCellback();
				}

				var ppStr = prettyPrintOne(response, 'java', true);
				$('#codetabcontents').append('<div class="tab-pane source-view" id="'+href+'">'+ppStr+'</div>');

				colorTaint(href);
				
				$('#codetabs a:last').tab('show');
				
			    var methodNames = $('#'+href).find("span[name=MethodName]");
			    for(var i=0; i < methodNames.length; ++i) {
			        $(methodNames[i]).after('<img src="/stamp/res/down.png" height="12" width="12" style="display:inline"></img>');
			        var reachable = $(methodNames[i]).attr("data-reachable");
					var reached = $(methodNames[i]).attr("reached");
			        if(reachable == "true")
			        	$(methodNames[i]).css('background','#BCF5A9' );
			        $(methodNames[i]).next().on("click",  function(event){
			  		  var chordSig = $(this).prev().attr("data-chordsig");
			  		  $('#rightbar').load('/stamp/html/imList.jsp',
			  		            {chordSig: chordSig, type: 'method'})
			  		});
			    }

			    var invkSites = $('#'+href).find("span[name=PreInvocation]");
			    for(var i=0; i < invkSites.length; ++i) {
			        $(invkSites[i]).append('<img src="/stamp/res/down.png" height="12" width="12" style="display:inline"></img>');
			        
			        $(invkSites[i]).on("click",  function(event){
			    		  var chordSig = $(this).attr("data-chordsig");
			    		  var filePath = $(this).attr("data-filePath");
			    		  var lineNum = $(this).attr("data-lineNum");
			    		  var drDataParams = ""
			    		  var invocationExpression = $(this).find(".invocationExpression")[0];
			    		  if(invocationExpression != null) {
			    		    drDataParams = invocationExpression.attr("data-droidrecord-params");
			    		  }
			    		  $('#rightbar').load('/stamp/html/imList.jsp',
			    		    {chordSig: chordSig, type: 'invk', filePath: filePath, lineNum: lineNum}, 
			    		    function () { rightBarAddDynamicData(drDataParams); })
			    		});
			    }
			    
			    function popoverAutoPlacement() {
			        var numVisiblePopOvers = $(".popover").size() % 4;
                    if(numVisiblePopOvers == 0) {
                        return 'bottom';
                    } else if(numVisiblePopOvers == 1) {
                        return 'top';
                    } else if(numVisiblePopOvers == 2) {
                        return 'left';
                    } else {
                        return 'right';
                    } 
			    }
			    
			    $(".invocationExpression").popover({
                        placement : popoverAutoPlacement, 
                        html : true,
                        title : function () {
                            var data = jQuery.parseJSON(atob($(this).attr("data-droidrecord-params")));
                            if(data == null) return "";
                            return data.methodName;
                        }, 
                        content : function() {
                            var data = jQuery.parseJSON(atob($(this).attr("data-droidrecord-params")));
			                if(data == null) return;
                            return ViewSource.droidrecordDataToTable(data, true);
                        }
                    });
			    
			    $(".srcSinkSpan").popover({
                        placement : popoverAutoPlacement, 
                        html : true,
                        title : function () {
                            return ""/*$(this).find("span[name=taintedVariable]").text()*/;
                        }, 
                        content : function() {
                            var data = jQuery.parseJSON(atob($(this).attr("data-stamp-srcsink")));
                            return ViewSource.formatStampSrcSinkInfo(data);
                        }
                    });
    
                $(".invocationExpression[data-droidrecord-params!=\"\"]").on("mouseenter",  function(event){
                    $(this).popover('show');
                    });
    
                $(".invocationExpression[data-droidrecord-params!=\"\"]").on("mouseleave",  function(event){
                    $(this).popover('hide');
                    });
    
                $(".srcSinkSpan").on("mouseenter",  function(event){
                    var srcSinkPopoverCount = $(".srcSinkSpan .popover").size();
                    if(srcSinkPopoverCount > 0) return;
                    $(this).popover('show');
                    });
    
                $(".srcSinkSpan").on("mouseleave",  function(event){
                    $(this).popover('hide');
                    });
    
                $(".invocationExpression[data-droidrecord-params!=\"\"]").on("click",  function(event){
                    var preinvk = $(this).find("span[name=PreInvocation]");
                    var chordSig = $(preinvk).attr("data-chordsig");
		            var filePath = $(preinvk).attr("data-filePath");
		            var lineNum = $(preinvk).attr("data-lineNum");
			    	var drDataParams = $(this).attr("data-droidrecord-params");
		            $('#rightbar').load('/stamp/html/imList.jsp',
		                {chordSig: chordSig, type: 'invk', filePath: filePath, lineNum: lineNum}, 
			    		function () { rightBarAddDynamicData(drDataParams); })
		            });

				var typeRefs = $('#'+href).find("[name=TypeRef]");
			    for(var i=0; i<typeRefs.length; ++i) {
					$(typeRefs[i]).on("click", function(event){
			    	   var chordSig = $(this).attr("data-chordsig");
			    	   $.ajax({
							type: "POST",
							url: "/stamp/html/getClassInfo.jsp",
							data: {chordsig: chordSig}
						}).done(function (response) {
							var tokens = $.trim(response).split(",");
							var filePath = tokens[0];
							var lineNum = tokens[1];
							showSource(filePath, 'false', lineNum, <%=useJimple%>);
						});	
                    });
			    }
			}		
			
			function highlightLine(ln, href)
			{
				if(typeof ln !== "undefined"){
					idToHighlightedLine[href] = ln;
					var scrollTo = $('#'+href+' ol li:nth-child('+ln+')');			
					var container = $('#'+href);
					container.scrollTop(
				    		scrollTo.offset().top - container.offset().top + container.scrollTop()
						);
					scrollTo.css('backgroundColor','#CEECF5' );
				}
			}
			
			var showContentTab = function(tabUniqueName, tabDisplayName, 
			                       onTabLoad, onTabDisplay)
            {
                if(!tabUniqueName)
                    return;
                $('#codetabs').show();
                var href = tabNameToId[tabUniqueName];
                if(typeof href === "undefined"){
                    //add a new tab
                    tabCount = $("#codetabs li").size(); 
                    var id = totalFilesOpened++;
                    href = 'filetab'+id;
                    tabNameToId[tabUniqueName] = href;

                    var tabTitle = tabDisplayName;
                    $('#codetabs').append('<li><a href="#'+href+'" data-toggle="tab">'+tabTitle+'<button class="btn btn-link" id="closetab'+href+'">x</button></a></li>');
                    $('#codetabs a:last button').on('click', function(event){
                        var tabToClose = $(this).attr('id').substring(8/*"closetab".length()*/);
                        var aNew;
                        var liCurrent;
                        var href = '#'+tabToClose;
							
                        $('#codetabs li a').each(function(){
                            if($(this).attr('href') == href){
                                liCurrent = $(this).parent();
                            } else if(typeof liNew === "undefined"){
                                aNew = $(this);
                            }
                        });
						
                        //delete the li from #codetabs
                        liCurrent.remove(); 

                        //delete the div from #codetabcontents
                        $('#'+tabToClose).remove(); 				
                        if(typeof aNew === "undefined")
                            $('#codetabs').hide();
                        else{
                            //alert(aNew.html());
                            aNew.tab('show');
                        }
						
                        //clean up
                        delete idToHighlightedLine[tabToClose];
                        for(var tn in tabNameToId) {
                            if(tabNameToId[tn] == tabToClose){
                                delete tabNameToId[tn];
                                break;
                            }
                        }
                    });
				    onTabLoad(href);
                } else {
                    $('#codetabs li a[href="#'+href+'"]').tab('show');
                }
                onTabDisplay(href);
            };
			
			var showSource = function(selectedFile, isModelFlag, ln, useJimple)
			{
			    var tabTitle = selectedFile.substring(selectedFile.lastIndexOf('/')+1);
			    var onTabLoad = function (href) {
                    $.ajax({
                        type: "POST",
                        url: "/stamp/html/viewSource.jsp",
                        data: {filepath: selectedFile, 
                            lineNum: ln, 
                            isModel: isModelFlag,
			    useJimple: useJimple}
	                }).done(function (response) {
                        showCode(response, href);
                        highlightLine(ln, href);
                    })
			    };
			    
			    var onTabDisplay = function (href) {
			        var highlightedLine = idToHighlightedLine[href];
					if(typeof highlightedLine !== "undefined"){
                        $('#'+href+' ol li:nth-child('+highlightedLine+')').css('backgroundColor','');
					}
					highlightLine(ln, href);
			    };
			    
			    showContentTab(selectedFile, tabTitle, onTabLoad, onTabDisplay);
			};
			
		</script>
			  
		<script>
			$('#AppHierarchy').on('selected', function(event,selection){
				showSource(selection.info[0].file, 'false', undefined, 'false');
			});

			$('#ModelsHierarchy').on('selected', function(event,selection){
				showSource(selection.info[0].file, 'true', undefined, 'false');
			});

			$('#FrameworkHierarchy').on('selected', function(event,selection){
				showSource(selection.info[0].file, 'false', undefined, 'false');
			});

			$('#JimpleHierarchy').on('selected', function(event,selection){
				showSource(selection.info[0].file, 'false', undefined, 'true');
			});
		</script>
		
		<script>
			var ResultDataSource = function(fname){
				this.resultFileName = fname;
			};

			ResultDataSource.prototype = {
					data : function(options, callback) {
						//for(var prop in options) alert(prop);
						//alert(this.resultFileName);
						$.ajax({
							type: "POST",
							url: "/stamp/html/viewResult.jsp",
							data: {resultFileName: this.resultFileName, nodeId: options.nodeId}
						}).done(function (response) {
							callback({data: $.parseJSON(response)});
							});
						}
			};

			var showReport = function(reportscript, reportfile, nodeid, shortname) 
			{
			    var tabUniqueName = reportscript + ":" + nodeid;
			    var onTabLoad = function (href) {
                    $.ajax({
                        type: "POST",
                        url: "/stamp/html/reportviews/" + reportscript + ".jsp",
                        data: {filepath: reportfile, 
                               id: nodeid}
	                }).done(function (response) {
                        $('#codetabcontents').append('<div class="tab-pane source-view" id="'+href+'">'+response+'</div>');
				        $('#codetabs a:last').tab('show');
                    })
			    };
			    
			    var onTabDisplay = function (href) {
			        // Do nothing
			    };
			    
			    showContentTab(tabUniqueName, shortname, onTabLoad, onTabDisplay);
			};

            var datasources = {};
			
			function setupResultTree(resultTreeId, resultFileName) {
			    useJimple = 'false';
			    //if(resultFileName.indexOf('jimple') != -1) {
			    if(<%=useJimple%>) {
			        useJimple = 'true';
			    }
                var datasource = new ResultDataSource(resultFileName);
			    $('#' + resultTreeId).tree({dataSource: dataSource});
                datasources[resultTreeId] = datasource;

			if(useJimple == 'true') {
			//alert('true: ' + resultFileName);
                $('#' + resultTreeId).on('selected', function(event,selection){
                    var reportScript = selection.info[0].showReport;
                    if(typeof reportScript === "undefined")
                    {
                        var file = selection.info[0].file;
                        var lineNum = selection.info[0].lineNum;
                        if(typeof file === "undefined")
                            return;
                        if(typeof lineNum === "undefined")
                            return;
                        showSource(file, 'false', lineNum, 'true');
                    }
				    else 
				    {
                        var nodeID = selection.info[0].nodeId;
                        var shortName = selection.info[0].reportNodeShortName;
                        if(typeof nodeID === "undefined")
                            return;
                        if(typeof shortName === "undefined")
                            return;
                        showReport(reportScript, resultFileName, nodeID, shortName);
                    }
                });
		} else {
			//alert('false: ' + resultFileName);
                $('#' + resultTreeId).on('selected', function(event,selection){
                    var reportScript = selection.info[0].showReport;
                    if(typeof reportScript === "undefined")
                    {
                        var file = selection.info[0].file;
                        var lineNum = selection.info[0].lineNum;
                        if(typeof file === "undefined")
                            return;
                        if(typeof lineNum === "undefined")
                            return;
                        showSource(file, 'false', lineNum, 'false');
                    }
				    else 
				    {
                        var nodeID = selection.info[0].nodeId;
                        var shortName = selection.info[0].reportNodeShortName;
                        if(typeof nodeID === "undefined")
                            return;
                        if(typeof shortName === "undefined")
                            return;
                        showReport(reportScript, resultFileName, nodeID, shortName);
                    }
                });
			}
			}

            /*
             * Hack for unescaping HTML strings. From CMS on stackoverflow
             * at http://stackoverflow.com/questions/1912501/unescape-html-entities-in-javascript
             */
            function htmlDecode(input){
              var e = document.createElement('div');
              e.innerHTML = input;
              return e.childNodes.length === 0 ? "" : e.childNodes[0].nodeValue;
            }

            function addSrcSinkFlowBehavior(id) {

                function escTags(str) {
                    var tagsToReplace = {
                        '&': '&amp;',
                        '<': '&lt;',
                        '>': '&gt;'
                    };

                    

                    function replaceTag(tag) {
                        return tagsToReplace[tag] || tag;
                    }

                    return str.replace(/[&<>]/g, replaceTag);
                }

                /* Inner function to create each new table row */
                function newTableEntries(clines) {

                        if (clines.length < 2) {
                            console.log("Error: incomplete context");
                            return;
                        }
                        var ctxtSplit = /(.+)~~~(.+)/;
                        var reSplit = /(.+),(.+)/;
                        var reEntry = /.*<.* (\S+ .*)>.*<.* (\S+ .*)>.*/;
                        var sourceline = htmlDecode(clines[0].name);
                        var sinkline = htmlDecode(clines[1].name);

                        if (!ctxtSplit.test(sourceline)) {
                            console.log("Error: Regex failure on context parse");
                            return;
                        }

                        var source = sourceline.match(ctxtSplit);
                        var sink = sinkline.match(ctxtSplit);

                        if (!reSplit.test(source[1]) || !reSplit.test(sink[1])) {
                            console.log("Error: Regex failure on context parse");
                            return;
                        }

                        var source_ctxts = source[1].match(reSplit);
                        var sink_ctxts = sink[1].match(reSplit);
                        console.log("lengths source "+source_ctxts.length + " sink "+sink_ctxts.length);
                        var entry = [];

                        var source_files = source[2].split('~');
                        var sink_files = sink[2].split('~');
            
                        for (var i = 1; i <= 2; ++i) {
                            var sourcem = source_ctxts[i].match(reEntry);
                            var sinkm = sink_ctxts[i].match(reEntry);
                            
                            for (var j = i; j <= 2; ++j) {
                                entry.push('<tr>');
                                entry.push('<td'+((source_files[j-1]!=='')?' source="'+source_files[j-1]+'"':'')+'>'+escTags(sourcem[j])+'</td>');
                                entry.push('<td'+((sink_files[j-1]!=='')?' source="'+sink_files[j-1]+'"':'')+'>'+escTags(sinkm[j])+'</td>');
                                entry.push('</tr>');
                            }
                        }
                        return entry.join('\n');;
                
                }

                $('#'+id).on('opened', function () {
	                    var $selected = $(this).find('.tree-folder-name').filter ( function () {
	                    	var flow_regex = /Flow (\d+)/;
		                    if ($(this).text() == '') {
		                        return true;
		                    }
	                    });
	                    $selected.parent().find('.icon-plus-sign').parent().html('<i class="icon-eye-open"></i>');
                });

                // on selected callback. Fuel UX provides selection
				$('#'+id).on('selected', function (ev, selection) {
					var $selected = $(this).find('.tree-folder-name').filter ( function () {
                        if ($(this).text() === selection.info[0].name) {
                            return true;
                        }
                    });

	                var datasource = datasources[id];

	                datasource.data($selected.parent().data(), function (items) {
	                                var dataarr = items.data;
	                                var contexts = [];
	                                for (var i = 0; i < dataarr.length; ++i) {
	                                    contexts.push(dataarr[i]);
	                                }

	                                var id = 'centerpane';
	                                if ($('li.active').length > 0) {
	                                	id = 'rightside';
	                                }

	                                if ($('#flowctxttable').length > 0) {
	                                    $('#flowctxttable').remove();
	                                }

	                                var table = ['<table class="table table-condensed" id="flowctxttable" style="font-size: small; word-break: break-all; word-wrap: break-word">',
	                                                '<thead>',
	                                                     '<th>Source</th>',
	                                                     '<th>Sink</th>',   
	                                                '</thead>',
	                                                '<tbody>'];
	                                table.push(newTableEntries(contexts));
	                                table.push('</tbody>');
	                                table.push('</table>');
	                                $('#'+id).append(table.join('\n'));
	                                if (id === 'rightside') {
	                                	compactFlowCtxtTable($('#'+id+' #flowctxttable'));
	                                }
	                                registerCellback();

	                            });

	            	});

                $('#'+id).on('click','i.icon-eye-open, i.icon-eye-close', function() {

	                	var $selected = $(this).parent().parent().find('.tree-folder-name');
	                	var name = $selected.text();
	                    var flow_regex = /Flow (\d+)/;
	                    if (!flow_regex.test(name)) {
	                        return;
	                    }
	                    var num = name.match(flow_regex)[1];

	                    if ($(this)[0].className === 'icon-eye-close') {
	                        $(this).parent().html('<i class="icon-eye-open"></i>');
	                        flowSwitches[num-1] = true;
	                        $('#srcsinkflowhelp').empty();
	                        $('#srcsinkflowhelp').append('Taint from Flow '+num+' now hightlighted.')
	                    } else {
	                        $(this).parent().html('<i class="icon-eye-close"></i>');
	                        flowSwitches[num-1] = false;
	                        $('#srcsinkflowhelp').empty();
	                        $('#srcsinkflowhelp').append('Not hightlighting taint from Flow '+num);
	                    }

		                var $activeCodeTabs = $('li.active a');
		                for (var i = 0; i < $activeCodeTabs.length; ++i) {
		                	var attr = $activeCodeTabs[i].getAttribute('href');
		                	colorTaint(attr);
		                }

                	
	                 });

				$('#'+id).parent().append('<p class="muted"><em id="srcsinkflowhelp">Click a Flow name to show / hide </em></p>');
            }

            function registerCellback() {
	            var $cells = $('#flowctxttable td');
	            $cells.click( function() {
		            	var source_str = $(this).attr('source');
		            	if (source_str !== '') {
			            	var source_splits = source_str.split(' ');
			            	showSource(source_splits[0],'false',source_splits[1]);
		            	}
	            	});
            }

			<%
			j = 0;
			for(Map.Entry<String,String> entry : titleToFileName.entrySet()){
				String title = entry.getKey();
				String resultFileName = entry.getValue();
				//if(!title.equals("Source-to-sink Flows")){
		    %>
			        setupResultTree('ResultTree<%=j%>', '<%=resultFileName%>');
			<%
                if (title.equals("Source-to-sink Flows")) {
            %>
                    addSrcSinkFlowBehavior('ResultTree<%=j%>');
			<%
                }
				//}
				j++;
			}
			%>		
		</script>	
	</body>
</html>
