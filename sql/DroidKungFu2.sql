 select distinct serviceId from
 (
 select distinct serviceId from
 (
   select node_id as recv_id, tgtId as serviceId, tgt_node_id as activity_id from (
     /*receiver a will launch b*/
       select node_id,  full_name, tgtId from ( 
       (select node_id, e.tgt_node_id as tgtId from intentFilter ift,  edge e 
                    where 
                                       ift.iccg_id=? and 
                                       (ift.name like '%BOOT_COMPLETE%') and 
                                        e.src_node_id=ift. node_id)  as tmp,

         /*service  b that generates src-sink*/
           (SELECT  distinct f1.src_node_id as serviceId  FROM flow as f1, flow as f2, flow as f3
                      where 
                                         f1.iccg_id=? and 
                                         f2.iccg_id=? and 
                                         f3.iccg_id=? and 
                                         f1.src_node_id=f2.src_node_id and 
                                         f2.src_node_id=f3.src_node_id and
                                         f1.source='$getDeviceId' and 
                                         f1.sink='!FILE' and  
                                         f2.source='$BRAND' and 
                                         f2.sink='!FILE' and  
                                         f3.source='$MODEL' 
                                         and f3.sink='!FILE') as tmp2,
             /*service b launches activity c. */
                node as nd) where 
                                                  node_id=nd.id and 
                                                  nd.type='receiver' and 
                                                  serviceId=tgtId) as tmp3, edge as eg, node as activity 
                                                              where 
                                                                        eg. src_node_id=tgtId and 
                                                                        eg.tgt_node_id=activity.id and 
                                                                        activity.type='activity'
                  /*install apk can be in other component. */
                  ) as tmp3, edge as eInstall, node as nInstall 
                                 where 
                                            eInstall.iccg_id=? and 
                                            eInstall.tgt_node_id=nInstall.id and 
                                            nInstall.full_name<>'INSTALL_APK'
                 /*encrypt c&c server and send it to internet*/
                  ) as tmp4 
                       inner join flow as f5 on (tmp4.serviceId=f5.src_node_id and f5.sink_node_id=f5.src_node_id and not(f5.source='$ENC/DEC' and f5.sink='!INTERNET')) 
   UNION
  /* Second cases.*/
  SELECT tmp5.actId from
  (SELECT  distinct f1.src_node_id as actId FROM flow as f1, flow as f2, flow as f3 
                      where 
                                         f1.iccg_id=? and 
                                         f2.iccg_id=? and 
                                         f3.iccg_id=? and 
                                         f1.src_node_id=f2.src_node_id and 
                                         f2.src_node_id=f3.src_node_id and
                                         f1.source='$getDeviceId' and 
                                         f1.sink='!FILE' and  
                                         f2.source='$BRAND' and 
                                         f2.sink='!FILE' and  
                                         f3.source='$MODEL' 
                                         and f3.sink='!FILE'
   ) as tmp5  left join node as act2 on act2.id=tmp5.actId 
      where 
            act2.type='activity'
