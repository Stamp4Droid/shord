
/*
            function sortFlows(flows) {

                for (flow in flows) {
                    newentry = '<tr><td>"+flow.srcLabel+"</td><td><i class="icon-arrow-right"></i></td><td>"+flow.sinkLabel+"</td><td><span class="label label-important">"+flow.modifier+"</span></td> \
                                <td><i class="icon-search"></i></td> \
                                <td><i class="icon-ok"></i></td> \
                                <td><i class="icon-ban-circle"></i></td> \
                                </tr> '

                    flowC = flow.flowClass;

                    if (flowC === "privacy") {
                        $("#privacy-rpt").append(newentry);

                    } else if (flowC === "integrity") {
                        $("integrity-rpt").append(newentry);

                    } else if (flowC === "other") {
                        // handle other

                    } else if (flowC === "") {
                        // handle null 

                    } else {
                        // handle something weird 
                        console.log("unknown flow class" + flowC);
                    }   

                    /*  
                        // other reports in overview.html that may need adding
                        } else if (flowC === "lowrisk") {

                        } else if (flowC === "warn") {

                        } else if (flowC === "coverage") {

                        }   
                    */  
/*                }   
            }   
*/
