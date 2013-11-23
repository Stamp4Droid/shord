/*iccg_id will be replaced dynamically.*/

select distinct serv.id from node as recv 
 inner join edge as recv_serv on recv.id=recv_serv.src_node_id 
 inner join node as serv on recv_serv.tgt_node_id=serv.id
 inner join intentFilter as ift on ift.node_id=recv.id
 inner join flow as f1 on f1.src_node_id=serv.id
 inner join flow as f2 on f2.src_node_id=serv.id
 inner join flow as f3 on f3.src_node_id=serv.id
 inner join callerComp as cc on cc.iccg_id=?
 where recv.iccg_id=? and recv.type='receiver'  and serv.type='service'  and (ift.name like '%SIG_STR')  and f1.source='$getDeviceId' and f1.sink='!INTERNET' and f2.source='$getSubscriberId' and f2.sink='!INTERNET' and f3.source='$getLine1Number' and f3.sink='!INTERNET' and cc.callee='<android.content.BroadcastReceiver: void abortBroadcast()>'

