select distinct tmp3.servId from
(
select e1.tgt_node_id as servId from
 (
select tmp.id as recvId, cc.callee as ce from 
 (
select * from node as recv,  intentFilter as ift where recv.iccg_id=? and recv.id=ift.node_id and ift.priority>1000 and (ift.name like '%BOOT_COMPLETED%') and (ift.name like '%SMS_RECEIVED%') and (ift.name like '%CONNECTIVITY_CHANGE%') and (ift.name like '%BATTERY_LOW%') and recv.type='receiver'
) as tmp left join callerComp as cc on cc.node_id=tmp.id
) as tmp2, edge as e1, node as serv where tmp2.ce='<android.content.BroadcastReceiver: void abortBroadcast()>' and e1.src_node_id=tmp2.recvId and serv.id=e1.tgt_node_id and serv.type='service'
) as tmp3, flow as f1, flow as f2, flow as f3, flow as f4 where tmp3.servId=f1.src_node_id and tmp3.servId=f2.src_node_id and tmp3.servId=f3.src_node_id and tmp3.servId=f4.src_node_id
 and f1.source='$content://sms' and f1.sink='!INTERNET' and  f2.source='$PRODUCT' and f2.sink='!INTERNET' and  f3.source='$getSubscriberId' and f3.sink='!INTERNET' and  f4.source='$MODEL' and f4.sink='!INTERNET'

