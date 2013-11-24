select distinct tmp1.servId from 
 (select serv.id as servId, serv.iccg_id from node as recv
    inner join intentFilter as ift on ift.node_id=recv.id
    inner join edge as e on e.src_node_id=recv.id
    inner join node as serv on serv.id=e.tgt_node_id
    where recv.iccg_id=? and
                   recv.type='receiver' and
                   ift.name='android.intent.action.BOOT_COMPLETED' and 
                   serv.type='service'
 ) as tmp1 
    inner join callerComp as cc on cc.iccg_id=tmp1.iccg_id
    inner join flow as f1 on f1.src_node_id=tmp1.servId
    inner join flow as f2 on f1.src_node_id=tmp1.servId
    inner join flow as f3 on f1.src_node_id=tmp1.servId
    inner join flow as f4 on f4.src_node_id=tmp1.servId
    where  f1.src_node_id=f1.sink_node_id and
                    f2.src_node_id=f2.sink_node_id  and 
                    f3.src_node_id=f3.sink_node_id and
                    f1.source='$getDeviceId' and
                    f1.sink='!File' and
                    f2.source='$getLine1Number' and
                    f2.sink='!File' and
                    f3.source='$SDK'  and
                    f3.sink='!File'  and 
                    f4.source='$File'  and
                    f4.sink='!WebView'  and 
                    cc.callee='<android.content.BroadcastReceiver: void abortBroadcast()>'

