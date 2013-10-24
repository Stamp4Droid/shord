package stamp.missingmodels.grammars;
import stamp.missingmodels.util.jcflsolver.*;

/* Original Grammar:
###################
# CONFIGURATION
###################

.output LabelRef3
.output LabelPrim3
.output Flow3

.weights refArg2RefArgTStub 1
.weights refArg2RefRetTStub 1

.weights primArg2RefArgTStub 1
.weights primArg2RefRetTStub 1

.weights refArg2PrimRetTStub 1
.weights prim2PrimT 1

.weights transfer 1

###################
# INPUTS
###################

# source annotations: src2RefT, src2PrimT
# sink annotations: sink2RefT, sink2PrimT, sinkF2RefF, sinkF2PrimF
# transfer annotations: ref2RefT, ref2PrimT, prim2RefT, prim2PrimT 
# flow annotations: ref2RefF, ref2PrimF, prim2RefF, prim2PrimF

# pt: pt, fptArr
# field: fpt
# helper: assignPrimCtxt, assignPrimCCtxt, loadPrimCtxtArr, storePrimCtxtArr
# field helper: loadPrimCtxt, loadStatPrimCtxt, storePrimCtxt, storeStatPrimCtxt

###################
# RULES: ANNOTATION CONVERSION
###################

# transfer annotations

Ref2RefT :: ref2RefT
Ref2PrimT :: ref2PrimT
Prim2RefT :: prim2RefT
Prim2PrimT :: prim2PrimT

Src2RefT :: src2RefT
Src2PrimT :: src2PrimT
Sink2RefT :: sink2RefT
Sink2PrimT :: sink2PrimT

# flow annotations

SinkF2RefF :: sinkF2RefF
SinkF2PrimF :: sinkF2PrimF

Ref2RefF :: ref2RefF
Ref2PrimF :: ref2PrimF
Prim2RefF :: prim2RefF
Prim2PrimF :: prim2PrimF

###################
# RULES: STUB ANNOTATION CONVERSION
###################

Ref2RefT :: refArg2RefArgTStub
Ref2RefT :: refArg2RefRetTStub

Prim2RefT :: primArg2RefArgTStub
Prim2RefT :: primArg2RefRetTStub

Ref2PrimT :: refArg2PrimRetTStub
Prim2PrimT :: primArg2PrimRetTStub

###################
# RULES: PARTIAL FLOW PROPAGATION
###################

PreFlowsTo :: preFlowsTo
PostFlowsTo :: postFlowsTo
MidFlowsTo :: midFlowsTo

PreFlowsTo :: PreFlowsTo transfer MidFlowsTo

Pt :: _PostFlowsTo _transfer _PreFlowsTo

Fpt[f] :: _Pt Store[f] Pt
FptArr :: _Pt StoreArr Pt

Pt :: pt

Fpt[f] :: fpt[f]
FptArr :: fptArr

###################
# RULES: OBJECT ANNOTATIONS
###################

Obj2RefT :: _Pt Ref2RefT
Obj2PrimT :: _Pt Ref2PrimT
Obj2RefT :: _FptArr Obj2RefT
Obj2PrimT :: _FptArr Obj2PrimT

Src2ObjT :: Src2RefT Pt
Src2ObjT :: Src2ObjT Fpt[*]

Sink2ObjT :: Sink2RefT Pt
Sink2ObjT :: Sink2ObjT Fpt[*]

###################
# RULES: SINKF
###################

# Sink_full-obj flow

SinkF2Obj :: SinkF2RefF Pt
SinkF2Obj :: Sink2Obj _Pt _Ref2RefF Pt
SinkF2Obj :: Sink2Prim _Ref2PrimF Pt
SinkF2Obj :: SinkF2Obj Fpt[*]

# Sink_full-prim flow

SinkF2Prim :: SinkF2PrimF
SinkF2Prim :: Sink2Obj _Pt _Prim2RefF
SinkF2Prim :: Sink2Prim _Prim2PrimF

###################
# RULES: SRC-SINK FLOW
###################

Src2Sink :: Src2Obj _SinkF2Obj
Src2Sink :: Src2Prim _SinkF2Prim
Src2Sink :: Src2PrimFld[*] _SinkF2Obj

###################
# RULES: LABEL FLOW
###################

# Src-obj flow

Src2Obj :: Src2ObjT
Src2Obj :: Src2ObjX

Src2ObjX :: Src2Obj Obj2RefT Pt
Src2ObjX :: Src2Prim Prim2RefT Pt
Src2ObjX :: Src2PrimFldArr Obj2RefT Pt
Src2ObjX :: Src2ObjX FptArr

# Sink-obj flow

Sink2Obj :: Sink2ObjT
Sink2Obj :: Sink2ObjX

Sink2ObjX :: Sink2Obj Obj2RefT Pt
Sink2ObjX :: Sink2Prim Prim2RefT Pt
Sink2ObjX :: Sink2PrimFldArr Obj2RefT Pt
Sink2ObjX :: Sink2ObjX FptArr

# Src-prim flow

Src2Prim :: Src2PrimT
Src2Prim :: Src2Prim _assignPrimCtxt
Src2Prim :: Src2Prim _assignPrimCCtxt

Src2Prim :: Src2Obj Obj2PrimT
Src2Prim :: Src2Prim Prim2PrimT

Src2Prim :: Src2ObjT _Pt _loadPrimCtxt[*]
Src2Prim :: Src2ObjX _Pt _loadPrimCtxtArr
Src2Prim :: Src2PrimFldArr Obj2PrimT

# cl Src2PrimFld[f] o _Pt v_c _loadPrimCtxt[f] u_c
Src2Prim :: Src2PrimFld[f] _Pt _loadPrimCtxt[f]
Src2Prim :: Src2PrimFldStat[f] _loadStatPrimCtxt[f]

# Src-prim_fld flow

Src2PrimFld[f] :: Src2Prim _storePrimCtxt[f] Pt
Src2PrimFldArr :: Src2Prim _storePrimCtxtArr Pt
Src2PrimFldStat[f] :: Src2Prim _storeStatPrimCtxt[f]

# Sink-prim flow

Sink2Prim :: Sink2PrimT
Sink2Prim :: Sink2Prim _assignPrimCtxt
Sink2Prim :: Sink2Prim _assignPrimCCtxt

Sink2Prim :: Sink2Obj Obj2PrimT
Sink2Prim :: Sink2Prim Prim2PrimT

Sink2Prim :: Sink2ObjT _Pt _loadPrimCtxt[*]
Sink2Prim :: Sink2ObjX _Pt _loadPrimCtxtArr
Sink2Prim :: Sink2PrimFldArr Obj2PrimT

# cl Sink2PrimFld[f] o _Pt v_c _loadPrimCtxt[f] u_c
Sink2Prim :: Sink2PrimFld[f] _Pt _loadPrimCtxt[f]
Sink2Prim :: Sink2PrimFldStat[f] _loadStatPrimCtxt[f]

# Sink-prim_fld flow

Sink2PrimFld[f] :: Sink2Prim _storePrimCtxt[f] Pt
Sink2PrimFldArr :: Sink2Prim _storePrimCtxtArr Pt
Sink2PrimFldStat[f] :: Sink2Prim _storeStatPrimCtxt[f]

###################
# RULES: OUTPUT
###################

LabelRef3 :: Src2Obj _Pt
LabelRef3 :: Sink2Obj _Pt
LabelPrim3 :: Src2Prim
LabelPrim3 :: Sink2Prim
Flow3 :: Src2Sink
*/

/* Normalized Grammar:
%13:
	%13 :: Sink2PrimFldArr Obj2RefT
PostFlowsTo:
	PostFlowsTo :: postFlowsTo
Pt:
	Pt :: %1 _PreFlowsTo
	Pt :: pt
Flow3:
	Flow3 :: Src2Sink
SinkF2Obj:
	SinkF2Obj :: SinkF2RefF Pt
	SinkF2Obj :: %5 Pt
	SinkF2Obj :: %6 Pt
	SinkF2Obj :: SinkF2Obj Fpt
Fpt:
	Fpt[i] :: %2[i] Pt
	Fpt[i] :: fpt[i]
Sink2PrimFldStat:
	Sink2PrimFldStat[i] :: Sink2Prim _storeStatPrimCtxt[i]
%9:
	%9 :: Src2Prim Prim2RefT
%8:
	%8 :: Src2Obj Obj2RefT
%4:
	%4 :: Sink2Obj _Pt
%5:
	%5 :: %4 _Ref2RefF
Src2PrimT:
	Src2PrimT :: src2PrimT
%7:
	%7 :: Sink2Obj _Pt
%6:
	%6 :: Sink2Prim _Ref2PrimF
%1:
	%1 :: _PostFlowsTo _transfer
%0:
	%0 :: PreFlowsTo transfer
%3:
	%3 :: _Pt StoreArr
%2:
	%2[i] :: _Pt Store[i]
Sink2PrimT:
	Sink2PrimT :: sink2PrimT
Src2PrimFldStat:
	Src2PrimFldStat[i] :: Src2Prim _storeStatPrimCtxt[i]
%20:
	%20 :: Sink2ObjX _Pt
%21:
	%21[i] :: Sink2PrimFld[i] _Pt
%22:
	%22[i] :: Sink2Prim _storePrimCtxt[i]
%23:
	%23 :: Sink2Prim _storePrimCtxtArr
Sink2Prim:
	Sink2Prim :: Sink2PrimT
	Sink2Prim :: Sink2Prim _assignPrimCtxt
	Sink2Prim :: Sink2Prim _assignPrimCCtxt
	Sink2Prim :: Sink2Obj Obj2PrimT
	Sink2Prim :: Sink2Prim Prim2PrimT
	Sink2Prim :: %19 _loadPrimCtxt
	Sink2Prim :: %20 _loadPrimCtxtArr
	Sink2Prim :: Sink2PrimFldArr Obj2PrimT
	Sink2Prim :: %21[i] _loadPrimCtxt[i]
	Sink2Prim :: Sink2PrimFldStat[i] _loadStatPrimCtxt[i]
Src2PrimFldArr:
	Src2PrimFldArr :: %18 Pt
FptArr:
	FptArr :: %3 Pt
	FptArr :: fptArr
SinkF2PrimF:
	SinkF2PrimF :: sinkF2PrimF
PreFlowsTo:
	PreFlowsTo :: preFlowsTo
	PreFlowsTo :: %0 MidFlowsTo
Sink2PrimFld:
	Sink2PrimFld[i] :: %22[i] Pt
%19:
	%19 :: Sink2ObjT _Pt
Src2Sink:
	Src2Sink :: Src2Obj _SinkF2Obj
	Src2Sink :: Src2Prim _SinkF2Prim
	Src2Sink :: Src2PrimFld _SinkF2Obj
%18:
	%18 :: Src2Prim _storePrimCtxtArr
Src2Prim:
	Src2Prim :: Src2PrimT
	Src2Prim :: Src2Prim _assignPrimCtxt
	Src2Prim :: Src2Prim _assignPrimCCtxt
	Src2Prim :: Src2Obj Obj2PrimT
	Src2Prim :: Src2Prim Prim2PrimT
	Src2Prim :: %14 _loadPrimCtxt
	Src2Prim :: %15 _loadPrimCtxtArr
	Src2Prim :: Src2PrimFldArr Obj2PrimT
	Src2Prim :: %16[i] _loadPrimCtxt[i]
	Src2Prim :: Src2PrimFldStat[i] _loadStatPrimCtxt[i]
Prim2PrimF:
	Prim2PrimF :: prim2PrimF
Sink2Obj:
	Sink2Obj :: Sink2ObjT
	Sink2Obj :: Sink2ObjX
Src2PrimFld:
	Src2PrimFld[i] :: %17[i] Pt
LabelPrim3:
	LabelPrim3 :: Src2Prim
	LabelPrim3 :: Sink2Prim
MidFlowsTo:
	MidFlowsTo :: midFlowsTo
Sink2ObjT:
	Sink2ObjT :: Sink2RefT Pt
	Sink2ObjT :: Sink2ObjT Fpt
Obj2PrimT:
	Obj2PrimT :: _Pt Ref2PrimT
	Obj2PrimT :: _FptArr Obj2PrimT
Sink2ObjX:
	Sink2ObjX :: %11 Pt
	Sink2ObjX :: %12 Pt
	Sink2ObjX :: %13 Pt
	Sink2ObjX :: Sink2ObjX FptArr
%14:
	%14 :: Src2ObjT _Pt
Prim2PrimT:
	Prim2PrimT :: prim2PrimT
	Prim2PrimT :: primArg2PrimRetTStub
Sink2PrimFldArr:
	Sink2PrimFldArr :: %23 Pt
Obj2RefT:
	Obj2RefT :: _Pt Ref2RefT
	Obj2RefT :: _FptArr Obj2RefT
Ref2PrimT:
	Ref2PrimT :: ref2PrimT
	Ref2PrimT :: refArg2PrimRetTStub
Src2ObjX:
	Src2ObjX :: %8 Pt
	Src2ObjX :: %9 Pt
	Src2ObjX :: %10 Pt
	Src2ObjX :: Src2ObjX FptArr
SinkF2Prim:
	SinkF2Prim :: SinkF2PrimF
	SinkF2Prim :: %7 _Prim2RefF
	SinkF2Prim :: Sink2Prim _Prim2PrimF
Ref2RefF:
	Ref2RefF :: ref2RefF
Sink2RefT:
	Sink2RefT :: sink2RefT
%11:
	%11 :: Sink2Obj Obj2RefT
%10:
	%10 :: Src2PrimFldArr Obj2RefT
Prim2RefT:
	Prim2RefT :: prim2RefT
	Prim2RefT :: primArg2RefArgTStub
	Prim2RefT :: primArg2RefRetTStub
%12:
	%12 :: Sink2Prim Prim2RefT
%15:
	%15 :: Src2ObjX _Pt
Src2ObjT:
	Src2ObjT :: Src2RefT Pt
	Src2ObjT :: Src2ObjT Fpt
%17:
	%17[i] :: Src2Prim _storePrimCtxt[i]
%16:
	%16[i] :: Src2PrimFld[i] _Pt
Ref2PrimF:
	Ref2PrimF :: ref2PrimF
Ref2RefT:
	Ref2RefT :: ref2RefT
	Ref2RefT :: refArg2RefArgTStub
	Ref2RefT :: refArg2RefRetTStub
Prim2RefF:
	Prim2RefF :: prim2RefF
SinkF2RefF:
	SinkF2RefF :: sinkF2RefF
Src2Obj:
	Src2Obj :: Src2ObjT
	Src2Obj :: Src2ObjX
Src2RefT:
	Src2RefT :: src2RefT
LabelRef3:
	LabelRef3 :: Src2Obj _Pt
	LabelRef3 :: Sink2Obj _Pt
*/

/* Reverse Productions:
storePrimCtxtArr:
	_storePrimCtxtArr + (Src2Prim *) => %18
	_storePrimCtxtArr + (Sink2Prim *) => %23
prim2RefT:
	prim2RefT => Prim2RefT
%9:
	%9 + (* Pt) => Src2ObjX
%8:
	%8 + (* Pt) => Src2ObjX
prim2RefF:
	prim2RefF => Prim2RefF
%4:
	%4 + (* _Ref2RefF) => %5
%7:
	%7 + (* _Prim2RefF) => SinkF2Prim
%6:
	%6 + (* Pt) => SinkF2Obj
%1:
	%1 + (* _PreFlowsTo) => Pt
%0:
	%0 + (* MidFlowsTo) => PreFlowsTo
%3:
	%3 + (* Pt) => FptArr
%2:
	%2[i] + (* Pt) => Fpt[i]
Src2PrimFldStat:
	Src2PrimFldStat[i] + (* _loadStatPrimCtxt[i]) => Src2Prim
postFlowsTo:
	postFlowsTo => PostFlowsTo
sink2RefT:
	sink2RefT => Sink2RefT
refArg2RefRetTStub:
	refArg2RefRetTStub => Ref2RefT
sink2PrimT:
	sink2PrimT => Sink2PrimT
storePrimCtxt:
	_storePrimCtxt[i] + (Src2Prim *) => %17[i]
	_storePrimCtxt[i] + (Sink2Prim *) => %22[i]
primArg2RefArgTStub:
	primArg2RefArgTStub => Prim2RefT
Src2PrimFld:
	Src2PrimFld + (* _SinkF2Obj) => Src2Sink
	Src2PrimFld[i] + (* _Pt) => %16[i]
MidFlowsTo:
	MidFlowsTo + (%0 *) => PreFlowsTo
preFlowsTo:
	preFlowsTo => PreFlowsTo
Src2ObjX:
	Src2ObjX => Src2Obj
	Src2ObjX + (* FptArr) => Src2ObjX
	Src2ObjX + (* _Pt) => %15
Ref2RefF:
	_Ref2RefF + (%4 *) => %5
assignPrimCCtxt:
	_assignPrimCCtxt + (Src2Prim *) => Src2Prim
	_assignPrimCCtxt + (Sink2Prim *) => Sink2Prim
SinkF2Obj:
	SinkF2Obj + (* Fpt) => SinkF2Obj
	_SinkF2Obj + (Src2Obj *) => Src2Sink
	_SinkF2Obj + (Src2PrimFld *) => Src2Sink
Ref2RefT:
	Ref2RefT + (_Pt *) => Obj2RefT
refArg2PrimRetTStub:
	refArg2PrimRetTStub => Ref2PrimT
Src2Obj:
	Src2Obj + (* _SinkF2Obj) => Src2Sink
	Src2Obj + (* Obj2RefT) => %8
	Src2Obj + (* Obj2PrimT) => Src2Prim
	Src2Obj + (* _Pt) => LabelRef3
ref2PrimT:
	ref2PrimT => Ref2PrimT
Pt:
	_Pt + (* Store[i]) => %2[i]
	Pt + (%2[i] *) => Fpt[i]
	_Pt + (* StoreArr) => %3
	Pt + (%3 *) => FptArr
	_Pt + (* Ref2RefT) => Obj2RefT
	_Pt + (* Ref2PrimT) => Obj2PrimT
	Pt + (Src2RefT *) => Src2ObjT
	Pt + (Sink2RefT *) => Sink2ObjT
	Pt + (SinkF2RefF *) => SinkF2Obj
	_Pt + (Sink2Obj *) => %4
	Pt + (%5 *) => SinkF2Obj
	Pt + (%6 *) => SinkF2Obj
	_Pt + (Sink2Obj *) => %7
	Pt + (%8 *) => Src2ObjX
	Pt + (%9 *) => Src2ObjX
	Pt + (%10 *) => Src2ObjX
	Pt + (%11 *) => Sink2ObjX
	Pt + (%12 *) => Sink2ObjX
	Pt + (%13 *) => Sink2ObjX
	_Pt + (Src2ObjT *) => %14
	_Pt + (Src2ObjX *) => %15
	_Pt + (Src2PrimFld[i] *) => %16[i]
	Pt + (%17[i] *) => Src2PrimFld[i]
	Pt + (%18 *) => Src2PrimFldArr
	_Pt + (Sink2ObjT *) => %19
	_Pt + (Sink2ObjX *) => %20
	_Pt + (Sink2PrimFld[i] *) => %21[i]
	Pt + (%22[i] *) => Sink2PrimFld[i]
	Pt + (%23 *) => Sink2PrimFldArr
	_Pt + (Src2Obj *) => LabelRef3
	_Pt + (Sink2Obj *) => LabelRef3
Src2ObjT:
	Src2ObjT + (* Fpt) => Src2ObjT
	Src2ObjT => Src2Obj
	Src2ObjT + (* _Pt) => %14
%5:
	%5 + (* Pt) => SinkF2Obj
ref2PrimF:
	ref2PrimF => Ref2PrimF
refArg2RefArgTStub:
	refArg2RefArgTStub => Ref2RefT
FptArr:
	_FptArr + (* Obj2RefT) => Obj2RefT
	_FptArr + (* Obj2PrimT) => Obj2PrimT
	FptArr + (Src2ObjX *) => Src2ObjX
	FptArr + (Sink2ObjX *) => Sink2ObjX
SinkF2PrimF:
	SinkF2PrimF => SinkF2Prim
Sink2PrimFld:
	Sink2PrimFld[i] + (* _Pt) => %21[i]
Src2Sink:
	Src2Sink => Flow3
loadStatPrimCtxt:
	_loadStatPrimCtxt[i] + (Src2PrimFldStat[i] *) => Src2Prim
	_loadStatPrimCtxt[i] + (Sink2PrimFldStat[i] *) => Sink2Prim
Prim2PrimF:
	_Prim2PrimF + (Sink2Prim *) => SinkF2Prim
Prim2PrimT:
	Prim2PrimT + (Src2Prim *) => Src2Prim
	Prim2PrimT + (Sink2Prim *) => Sink2Prim
storeStatPrimCtxt:
	_storeStatPrimCtxt[i] + (Src2Prim *) => Src2PrimFldStat[i]
	_storeStatPrimCtxt[i] + (Sink2Prim *) => Sink2PrimFldStat[i]
Ref2PrimT:
	Ref2PrimT + (_Pt *) => Obj2PrimT
SinkF2Prim:
	_SinkF2Prim + (Src2Prim *) => Src2Sink
loadPrimCtxtArr:
	_loadPrimCtxtArr + (%15 *) => Src2Prim
	_loadPrimCtxtArr + (%20 *) => Sink2Prim
StoreArr:
	StoreArr + (_Pt *) => %3
Ref2PrimF:
	_Ref2PrimF + (Sink2Prim *) => %6
PostFlowsTo:
	_PostFlowsTo + (* _transfer) => %1
assignPrimCtxt:
	_assignPrimCtxt + (Src2Prim *) => Src2Prim
	_assignPrimCtxt + (Sink2Prim *) => Sink2Prim
fptArr:
	fptArr => FptArr
sinkF2PrimF:
	sinkF2PrimF => SinkF2PrimF
Src2PrimT:
	Src2PrimT => Src2Prim
Store:
	Store[i] + (_Pt *) => %2[i]
pt:
	pt => Pt
transfer:
	transfer + (PreFlowsTo *) => %0
	_transfer + (_PostFlowsTo *) => %1
Sink2PrimFldArr:
	Sink2PrimFldArr + (* Obj2RefT) => %13
	Sink2PrimFldArr + (* Obj2PrimT) => Sink2Prim
sinkF2RefF:
	sinkF2RefF => SinkF2RefF
Sink2RefT:
	Sink2RefT + (* Pt) => Sink2ObjT
src2PrimT:
	src2PrimT => Src2PrimT
Obj2PrimT:
	Obj2PrimT + (_FptArr *) => Obj2PrimT
	Obj2PrimT + (Src2Obj *) => Src2Prim
	Obj2PrimT + (Src2PrimFldArr *) => Src2Prim
	Obj2PrimT + (Sink2Obj *) => Sink2Prim
	Obj2PrimT + (Sink2PrimFldArr *) => Sink2Prim
loadPrimCtxt:
	_loadPrimCtxt + (%14 *) => Src2Prim
	_loadPrimCtxt[i] + (%16[i] *) => Src2Prim
	_loadPrimCtxt + (%19 *) => Sink2Prim
	_loadPrimCtxt[i] + (%21[i] *) => Sink2Prim
Obj2RefT:
	Obj2RefT + (_FptArr *) => Obj2RefT
	Obj2RefT + (Src2Obj *) => %8
	Obj2RefT + (Src2PrimFldArr *) => %10
	Obj2RefT + (Sink2Obj *) => %11
	Obj2RefT + (Sink2PrimFldArr *) => %13
Src2Prim:
	Src2Prim + (* _SinkF2Prim) => Src2Sink
	Src2Prim + (* Prim2RefT) => %9
	Src2Prim + (* _assignPrimCtxt) => Src2Prim
	Src2Prim + (* _assignPrimCCtxt) => Src2Prim
	Src2Prim + (* Prim2PrimT) => Src2Prim
	Src2Prim + (* _storePrimCtxt[i]) => %17[i]
	Src2Prim + (* _storePrimCtxtArr) => %18
	Src2Prim + (* _storeStatPrimCtxt[i]) => Src2PrimFldStat[i]
	Src2Prim => LabelPrim3
Fpt:
	Fpt + (Src2ObjT *) => Src2ObjT
	Fpt + (Sink2ObjT *) => Sink2ObjT
	Fpt + (SinkF2Obj *) => SinkF2Obj
Prim2RefT:
	Prim2RefT + (Src2Prim *) => %9
	Prim2RefT + (Sink2Prim *) => %12
prim2PrimF:
	prim2PrimF => Prim2PrimF
midFlowsTo:
	midFlowsTo => MidFlowsTo
Prim2RefF:
	_Prim2RefF + (%7 *) => SinkF2Prim
prim2PrimT:
	prim2PrimT => Prim2PrimT
Src2RefT:
	Src2RefT + (* Pt) => Src2ObjT
ref2RefF:
	ref2RefF => Ref2RefF
primArg2RefRetTStub:
	primArg2RefRetTStub => Prim2RefT
Sink2PrimFldStat:
	Sink2PrimFldStat[i] + (* _loadStatPrimCtxt[i]) => Sink2Prim
ref2RefT:
	ref2RefT => Ref2RefT
Sink2Prim:
	Sink2Prim + (* _Ref2PrimF) => %6
	Sink2Prim + (* _Prim2PrimF) => SinkF2Prim
	Sink2Prim + (* Prim2RefT) => %12
	Sink2Prim + (* _assignPrimCtxt) => Sink2Prim
	Sink2Prim + (* _assignPrimCCtxt) => Sink2Prim
	Sink2Prim + (* Prim2PrimT) => Sink2Prim
	Sink2Prim + (* _storePrimCtxt[i]) => %22[i]
	Sink2Prim + (* _storePrimCtxtArr) => %23
	Sink2Prim + (* _storeStatPrimCtxt[i]) => Sink2PrimFldStat[i]
	Sink2Prim => LabelPrim3
primArg2PrimRetTStub:
	primArg2PrimRetTStub => Prim2PrimT
Src2PrimFldArr:
	Src2PrimFldArr + (* Obj2RefT) => %10
	Src2PrimFldArr + (* Obj2PrimT) => Src2Prim
%20:
	%20 + (* _loadPrimCtxtArr) => Sink2Prim
%21:
	%21[i] + (* _loadPrimCtxt[i]) => Sink2Prim
%22:
	%22[i] + (* Pt) => Sink2PrimFld[i]
%23:
	%23 + (* Pt) => Sink2PrimFldArr
src2RefT:
	src2RefT => Src2RefT
fpt:
	fpt[i] => Fpt[i]
PreFlowsTo:
	PreFlowsTo + (* transfer) => %0
	_PreFlowsTo + (%1 *) => Pt
Sink2PrimT:
	Sink2PrimT => Sink2Prim
Sink2Obj:
	Sink2Obj + (* _Pt) => %4
	Sink2Obj + (* _Pt) => %7
	Sink2Obj + (* Obj2RefT) => %11
	Sink2Obj + (* Obj2PrimT) => Sink2Prim
	Sink2Obj + (* _Pt) => LabelRef3
SinkF2RefF:
	SinkF2RefF + (* Pt) => SinkF2Obj
Sink2ObjT:
	Sink2ObjT + (* Fpt) => Sink2ObjT
	Sink2ObjT => Sink2Obj
	Sink2ObjT + (* _Pt) => %19
Sink2ObjX:
	Sink2ObjX => Sink2Obj
	Sink2ObjX + (* FptArr) => Sink2ObjX
	Sink2ObjX + (* _Pt) => %20
%19:
	%19 + (* _loadPrimCtxt) => Sink2Prim
%18:
	%18 + (* Pt) => Src2PrimFldArr
%11:
	%11 + (* Pt) => Sink2ObjX
%10:
	%10 + (* Pt) => Src2ObjX
%13:
	%13 + (* Pt) => Sink2ObjX
%12:
	%12 + (* Pt) => Sink2ObjX
%15:
	%15 + (* _loadPrimCtxtArr) => Src2Prim
%14:
	%14 + (* _loadPrimCtxt) => Src2Prim
%17:
	%17[i] + (* Pt) => Src2PrimFld[i]
%16:
	%16[i] + (* _loadPrimCtxt[i]) => Src2Prim
*/

public class G extends Graph {

public boolean isTerminal(int kind) {
  switch (kind) {
  case 1:
  case 3:
  case 5:
  case 7:
  case 9:
  case 11:
  case 13:
  case 15:
  case 17:
  case 19:
  case 21:
  case 23:
  case 25:
  case 27:
  case 28:
  case 29:
  case 30:
  case 31:
  case 32:
  case 33:
  case 35:
  case 37:
  case 39:
  case 40:
  case 50:
  case 51:
  case 52:
  case 79:
  case 80:
  case 81:
  case 83:
  case 87:
  case 88:
  case 90:
  case 92:
    return true;
  default:
    return false;
  }
}

public int numKinds() {
  return 103;
}

public int symbolToKind(String symbol) {
  if (symbol.equals("Ref2RefT")) return 0;
  if (symbol.equals("ref2RefT")) return 1;
  if (symbol.equals("Ref2PrimT")) return 2;
  if (symbol.equals("ref2PrimT")) return 3;
  if (symbol.equals("Prim2RefT")) return 4;
  if (symbol.equals("prim2RefT")) return 5;
  if (symbol.equals("Prim2PrimT")) return 6;
  if (symbol.equals("prim2PrimT")) return 7;
  if (symbol.equals("Src2RefT")) return 8;
  if (symbol.equals("src2RefT")) return 9;
  if (symbol.equals("Src2PrimT")) return 10;
  if (symbol.equals("src2PrimT")) return 11;
  if (symbol.equals("Sink2RefT")) return 12;
  if (symbol.equals("sink2RefT")) return 13;
  if (symbol.equals("Sink2PrimT")) return 14;
  if (symbol.equals("sink2PrimT")) return 15;
  if (symbol.equals("SinkF2RefF")) return 16;
  if (symbol.equals("sinkF2RefF")) return 17;
  if (symbol.equals("SinkF2PrimF")) return 18;
  if (symbol.equals("sinkF2PrimF")) return 19;
  if (symbol.equals("Ref2RefF")) return 20;
  if (symbol.equals("ref2RefF")) return 21;
  if (symbol.equals("Ref2PrimF")) return 22;
  if (symbol.equals("ref2PrimF")) return 23;
  if (symbol.equals("Prim2RefF")) return 24;
  if (symbol.equals("prim2RefF")) return 25;
  if (symbol.equals("Prim2PrimF")) return 26;
  if (symbol.equals("prim2PrimF")) return 27;
  if (symbol.equals("refArg2RefArgTStub")) return 28;
  if (symbol.equals("refArg2RefRetTStub")) return 29;
  if (symbol.equals("primArg2RefArgTStub")) return 30;
  if (symbol.equals("primArg2RefRetTStub")) return 31;
  if (symbol.equals("refArg2PrimRetTStub")) return 32;
  if (symbol.equals("primArg2PrimRetTStub")) return 33;
  if (symbol.equals("PreFlowsTo")) return 34;
  if (symbol.equals("preFlowsTo")) return 35;
  if (symbol.equals("PostFlowsTo")) return 36;
  if (symbol.equals("postFlowsTo")) return 37;
  if (symbol.equals("MidFlowsTo")) return 38;
  if (symbol.equals("midFlowsTo")) return 39;
  if (symbol.equals("transfer")) return 40;
  if (symbol.equals("%0")) return 41;
  if (symbol.equals("Pt")) return 42;
  if (symbol.equals("%1")) return 43;
  if (symbol.equals("Fpt")) return 44;
  if (symbol.equals("Store")) return 45;
  if (symbol.equals("%2")) return 46;
  if (symbol.equals("FptArr")) return 47;
  if (symbol.equals("StoreArr")) return 48;
  if (symbol.equals("%3")) return 49;
  if (symbol.equals("pt")) return 50;
  if (symbol.equals("fpt")) return 51;
  if (symbol.equals("fptArr")) return 52;
  if (symbol.equals("Obj2RefT")) return 53;
  if (symbol.equals("Obj2PrimT")) return 54;
  if (symbol.equals("Src2ObjT")) return 55;
  if (symbol.equals("Sink2ObjT")) return 56;
  if (symbol.equals("SinkF2Obj")) return 57;
  if (symbol.equals("Sink2Obj")) return 58;
  if (symbol.equals("%4")) return 59;
  if (symbol.equals("%5")) return 60;
  if (symbol.equals("Sink2Prim")) return 61;
  if (symbol.equals("%6")) return 62;
  if (symbol.equals("SinkF2Prim")) return 63;
  if (symbol.equals("%7")) return 64;
  if (symbol.equals("Src2Sink")) return 65;
  if (symbol.equals("Src2Obj")) return 66;
  if (symbol.equals("Src2Prim")) return 67;
  if (symbol.equals("Src2PrimFld")) return 68;
  if (symbol.equals("Src2ObjX")) return 69;
  if (symbol.equals("%8")) return 70;
  if (symbol.equals("%9")) return 71;
  if (symbol.equals("Src2PrimFldArr")) return 72;
  if (symbol.equals("%10")) return 73;
  if (symbol.equals("Sink2ObjX")) return 74;
  if (symbol.equals("%11")) return 75;
  if (symbol.equals("%12")) return 76;
  if (symbol.equals("Sink2PrimFldArr")) return 77;
  if (symbol.equals("%13")) return 78;
  if (symbol.equals("assignPrimCtxt")) return 79;
  if (symbol.equals("assignPrimCCtxt")) return 80;
  if (symbol.equals("loadPrimCtxt")) return 81;
  if (symbol.equals("%14")) return 82;
  if (symbol.equals("loadPrimCtxtArr")) return 83;
  if (symbol.equals("%15")) return 84;
  if (symbol.equals("%16")) return 85;
  if (symbol.equals("Src2PrimFldStat")) return 86;
  if (symbol.equals("loadStatPrimCtxt")) return 87;
  if (symbol.equals("storePrimCtxt")) return 88;
  if (symbol.equals("%17")) return 89;
  if (symbol.equals("storePrimCtxtArr")) return 90;
  if (symbol.equals("%18")) return 91;
  if (symbol.equals("storeStatPrimCtxt")) return 92;
  if (symbol.equals("%19")) return 93;
  if (symbol.equals("%20")) return 94;
  if (symbol.equals("Sink2PrimFld")) return 95;
  if (symbol.equals("%21")) return 96;
  if (symbol.equals("Sink2PrimFldStat")) return 97;
  if (symbol.equals("%22")) return 98;
  if (symbol.equals("%23")) return 99;
  if (symbol.equals("LabelRef3")) return 100;
  if (symbol.equals("LabelPrim3")) return 101;
  if (symbol.equals("Flow3")) return 102;
  throw new RuntimeException("Unknown symbol "+symbol);
}

public String kindToSymbol(int kind) {
  switch (kind) {
  case 0: return "Ref2RefT";
  case 1: return "ref2RefT";
  case 2: return "Ref2PrimT";
  case 3: return "ref2PrimT";
  case 4: return "Prim2RefT";
  case 5: return "prim2RefT";
  case 6: return "Prim2PrimT";
  case 7: return "prim2PrimT";
  case 8: return "Src2RefT";
  case 9: return "src2RefT";
  case 10: return "Src2PrimT";
  case 11: return "src2PrimT";
  case 12: return "Sink2RefT";
  case 13: return "sink2RefT";
  case 14: return "Sink2PrimT";
  case 15: return "sink2PrimT";
  case 16: return "SinkF2RefF";
  case 17: return "sinkF2RefF";
  case 18: return "SinkF2PrimF";
  case 19: return "sinkF2PrimF";
  case 20: return "Ref2RefF";
  case 21: return "ref2RefF";
  case 22: return "Ref2PrimF";
  case 23: return "ref2PrimF";
  case 24: return "Prim2RefF";
  case 25: return "prim2RefF";
  case 26: return "Prim2PrimF";
  case 27: return "prim2PrimF";
  case 28: return "refArg2RefArgTStub";
  case 29: return "refArg2RefRetTStub";
  case 30: return "primArg2RefArgTStub";
  case 31: return "primArg2RefRetTStub";
  case 32: return "refArg2PrimRetTStub";
  case 33: return "primArg2PrimRetTStub";
  case 34: return "PreFlowsTo";
  case 35: return "preFlowsTo";
  case 36: return "PostFlowsTo";
  case 37: return "postFlowsTo";
  case 38: return "MidFlowsTo";
  case 39: return "midFlowsTo";
  case 40: return "transfer";
  case 41: return "%0";
  case 42: return "Pt";
  case 43: return "%1";
  case 44: return "Fpt";
  case 45: return "Store";
  case 46: return "%2";
  case 47: return "FptArr";
  case 48: return "StoreArr";
  case 49: return "%3";
  case 50: return "pt";
  case 51: return "fpt";
  case 52: return "fptArr";
  case 53: return "Obj2RefT";
  case 54: return "Obj2PrimT";
  case 55: return "Src2ObjT";
  case 56: return "Sink2ObjT";
  case 57: return "SinkF2Obj";
  case 58: return "Sink2Obj";
  case 59: return "%4";
  case 60: return "%5";
  case 61: return "Sink2Prim";
  case 62: return "%6";
  case 63: return "SinkF2Prim";
  case 64: return "%7";
  case 65: return "Src2Sink";
  case 66: return "Src2Obj";
  case 67: return "Src2Prim";
  case 68: return "Src2PrimFld";
  case 69: return "Src2ObjX";
  case 70: return "%8";
  case 71: return "%9";
  case 72: return "Src2PrimFldArr";
  case 73: return "%10";
  case 74: return "Sink2ObjX";
  case 75: return "%11";
  case 76: return "%12";
  case 77: return "Sink2PrimFldArr";
  case 78: return "%13";
  case 79: return "assignPrimCtxt";
  case 80: return "assignPrimCCtxt";
  case 81: return "loadPrimCtxt";
  case 82: return "%14";
  case 83: return "loadPrimCtxtArr";
  case 84: return "%15";
  case 85: return "%16";
  case 86: return "Src2PrimFldStat";
  case 87: return "loadStatPrimCtxt";
  case 88: return "storePrimCtxt";
  case 89: return "%17";
  case 90: return "storePrimCtxtArr";
  case 91: return "%18";
  case 92: return "storeStatPrimCtxt";
  case 93: return "%19";
  case 94: return "%20";
  case 95: return "Sink2PrimFld";
  case 96: return "%21";
  case 97: return "Sink2PrimFldStat";
  case 98: return "%22";
  case 99: return "%23";
  case 100: return "LabelRef3";
  case 101: return "LabelPrim3";
  case 102: return "Flow3";
  default: throw new RuntimeException("Unknown kind "+kind);
  }
}

public void process(Edge base) {
  switch (base.kind) {
  case 0: /* Ref2RefT */
    /* Ref2RefT + (_Pt *) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(42)){
      addEdge(other.to, base.to, 53, base, other, false);
    }
    break;
  case 1: /* ref2RefT */
    /* ref2RefT => Ref2RefT */
    addEdge(base.from, base.to, 0, base, false);
    break;
  case 2: /* Ref2PrimT */
    /* Ref2PrimT + (_Pt *) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(42)){
      addEdge(other.to, base.to, 54, base, other, false);
    }
    break;
  case 3: /* ref2PrimT */
    /* ref2PrimT => Ref2PrimT */
    addEdge(base.from, base.to, 2, base, false);
    break;
  case 4: /* Prim2RefT */
    /* Prim2RefT + (Src2Prim *) => %9 */
    for(Edge other : base.from.getInEdges(67)){
      addEdge(other.from, base.to, 71, base, other, false);
    }
    /* Prim2RefT + (Sink2Prim *) => %12 */
    for(Edge other : base.from.getInEdges(61)){
      addEdge(other.from, base.to, 76, base, other, false);
    }
    break;
  case 5: /* prim2RefT */
    /* prim2RefT => Prim2RefT */
    addEdge(base.from, base.to, 4, base, false);
    break;
  case 6: /* Prim2PrimT */
    /* Prim2PrimT + (Src2Prim *) => Src2Prim */
    for(Edge other : base.from.getInEdges(67)){
      addEdge(other.from, base.to, 67, base, other, false);
    }
    /* Prim2PrimT + (Sink2Prim *) => Sink2Prim */
    for(Edge other : base.from.getInEdges(61)){
      addEdge(other.from, base.to, 61, base, other, false);
    }
    break;
  case 7: /* prim2PrimT */
    /* prim2PrimT => Prim2PrimT */
    addEdge(base.from, base.to, 6, base, false);
    break;
  case 8: /* Src2RefT */
    /* Src2RefT + (* Pt) => Src2ObjT */
    for(Edge other : base.to.getOutEdges(42)){
      addEdge(base.from, other.to, 55, base, other, false);
    }
    break;
  case 9: /* src2RefT */
    /* src2RefT => Src2RefT */
    addEdge(base.from, base.to, 8, base, false);
    break;
  case 10: /* Src2PrimT */
    /* Src2PrimT => Src2Prim */
    addEdge(base.from, base.to, 67, base, false);
    break;
  case 11: /* src2PrimT */
    /* src2PrimT => Src2PrimT */
    addEdge(base.from, base.to, 10, base, false);
    break;
  case 12: /* Sink2RefT */
    /* Sink2RefT + (* Pt) => Sink2ObjT */
    for(Edge other : base.to.getOutEdges(42)){
      addEdge(base.from, other.to, 56, base, other, false);
    }
    break;
  case 13: /* sink2RefT */
    /* sink2RefT => Sink2RefT */
    addEdge(base.from, base.to, 12, base, false);
    break;
  case 14: /* Sink2PrimT */
    /* Sink2PrimT => Sink2Prim */
    addEdge(base.from, base.to, 61, base, false);
    break;
  case 15: /* sink2PrimT */
    /* sink2PrimT => Sink2PrimT */
    addEdge(base.from, base.to, 14, base, false);
    break;
  case 16: /* SinkF2RefF */
    /* SinkF2RefF + (* Pt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(42)){
      addEdge(base.from, other.to, 57, base, other, false);
    }
    break;
  case 17: /* sinkF2RefF */
    /* sinkF2RefF => SinkF2RefF */
    addEdge(base.from, base.to, 16, base, false);
    break;
  case 18: /* SinkF2PrimF */
    /* SinkF2PrimF => SinkF2Prim */
    addEdge(base.from, base.to, 63, base, false);
    break;
  case 19: /* sinkF2PrimF */
    /* sinkF2PrimF => SinkF2PrimF */
    addEdge(base.from, base.to, 18, base, false);
    break;
  case 20: /* Ref2RefF */
    /* _Ref2RefF + (%4 *) => %5 */
    for(Edge other : base.to.getInEdges(59)){
      addEdge(other.from, base.from, 60, base, other, false);
    }
    break;
  case 21: /* ref2RefF */
    /* ref2RefF => Ref2RefF */
    addEdge(base.from, base.to, 20, base, false);
    break;
  case 22: /* Ref2PrimF */
    /* _Ref2PrimF + (Sink2Prim *) => %6 */
    for(Edge other : base.to.getInEdges(61)){
      addEdge(other.from, base.from, 62, base, other, false);
    }
    break;
  case 23: /* ref2PrimF */
    /* ref2PrimF => Ref2PrimF */
    addEdge(base.from, base.to, 22, base, false);
    break;
  case 24: /* Prim2RefF */
    /* _Prim2RefF + (%7 *) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(64)){
      addEdge(other.from, base.from, 63, base, other, false);
    }
    break;
  case 25: /* prim2RefF */
    /* prim2RefF => Prim2RefF */
    addEdge(base.from, base.to, 24, base, false);
    break;
  case 26: /* Prim2PrimF */
    /* _Prim2PrimF + (Sink2Prim *) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(61)){
      addEdge(other.from, base.from, 63, base, other, false);
    }
    break;
  case 27: /* prim2PrimF */
    /* prim2PrimF => Prim2PrimF */
    addEdge(base.from, base.to, 26, base, false);
    break;
  case 28: /* refArg2RefArgTStub */
    /* refArg2RefArgTStub => Ref2RefT */
    addEdge(base.from, base.to, 0, base, false);
    break;
  case 29: /* refArg2RefRetTStub */
    /* refArg2RefRetTStub => Ref2RefT */
    addEdge(base.from, base.to, 0, base, false);
    break;
  case 30: /* primArg2RefArgTStub */
    /* primArg2RefArgTStub => Prim2RefT */
    addEdge(base.from, base.to, 4, base, false);
    break;
  case 31: /* primArg2RefRetTStub */
    /* primArg2RefRetTStub => Prim2RefT */
    addEdge(base.from, base.to, 4, base, false);
    break;
  case 32: /* refArg2PrimRetTStub */
    /* refArg2PrimRetTStub => Ref2PrimT */
    addEdge(base.from, base.to, 2, base, false);
    break;
  case 33: /* primArg2PrimRetTStub */
    /* primArg2PrimRetTStub => Prim2PrimT */
    addEdge(base.from, base.to, 6, base, false);
    break;
  case 34: /* PreFlowsTo */
    /* PreFlowsTo + (* transfer) => %0 */
    for(Edge other : base.to.getOutEdges(40)){
      addEdge(base.from, other.to, 41, base, other, false);
    }
    /* _PreFlowsTo + (%1 *) => Pt */
    for(Edge other : base.to.getInEdges(43)){
      addEdge(other.from, base.from, 42, base, other, false);
    }
    break;
  case 35: /* preFlowsTo */
    /* preFlowsTo => PreFlowsTo */
    addEdge(base.from, base.to, 34, base, false);
    break;
  case 36: /* PostFlowsTo */
    /* _PostFlowsTo + (* _transfer) => %1 */
    for(Edge other : base.from.getInEdges(40)){
      addEdge(base.to, other.from, 43, base, other, false);
    }
    break;
  case 37: /* postFlowsTo */
    /* postFlowsTo => PostFlowsTo */
    addEdge(base.from, base.to, 36, base, false);
    break;
  case 38: /* MidFlowsTo */
    /* MidFlowsTo + (%0 *) => PreFlowsTo */
    for(Edge other : base.from.getInEdges(41)){
      addEdge(other.from, base.to, 34, base, other, false);
    }
    break;
  case 39: /* midFlowsTo */
    /* midFlowsTo => MidFlowsTo */
    addEdge(base.from, base.to, 38, base, false);
    break;
  case 40: /* transfer */
    /* transfer + (PreFlowsTo *) => %0 */
    for(Edge other : base.from.getInEdges(34)){
      addEdge(other.from, base.to, 41, base, other, false);
    }
    /* _transfer + (_PostFlowsTo *) => %1 */
    for(Edge other : base.to.getOutEdges(36)){
      addEdge(other.to, base.from, 43, base, other, false);
    }
    break;
  case 41: /* %0 */
    /* %0 + (* MidFlowsTo) => PreFlowsTo */
    for(Edge other : base.to.getOutEdges(38)){
      addEdge(base.from, other.to, 34, base, other, false);
    }
    break;
  case 42: /* Pt */
    /* _Pt + (* Store[i]) => %2[i] */
    for(Edge other : base.from.getOutEdges(45)){
      addEdge(base.to, other.to, 46, base, other, true);
    }
    /* Pt + (%2[i] *) => Fpt[i] */
    for(Edge other : base.from.getInEdges(46)){
      addEdge(other.from, base.to, 44, base, other, true);
    }
    /* _Pt + (* StoreArr) => %3 */
    for(Edge other : base.from.getOutEdges(48)){
      addEdge(base.to, other.to, 49, base, other, false);
    }
    /* Pt + (%3 *) => FptArr */
    for(Edge other : base.from.getInEdges(49)){
      addEdge(other.from, base.to, 47, base, other, false);
    }
    /* _Pt + (* Ref2RefT) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(0)){
      addEdge(base.to, other.to, 53, base, other, false);
    }
    /* _Pt + (* Ref2PrimT) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(2)){
      addEdge(base.to, other.to, 54, base, other, false);
    }
    /* Pt + (Src2RefT *) => Src2ObjT */
    for(Edge other : base.from.getInEdges(8)){
      addEdge(other.from, base.to, 55, base, other, false);
    }
    /* Pt + (Sink2RefT *) => Sink2ObjT */
    for(Edge other : base.from.getInEdges(12)){
      addEdge(other.from, base.to, 56, base, other, false);
    }
    /* Pt + (SinkF2RefF *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(16)){
      addEdge(other.from, base.to, 57, base, other, false);
    }
    /* _Pt + (Sink2Obj *) => %4 */
    for(Edge other : base.to.getInEdges(58)){
      addEdge(other.from, base.from, 59, base, other, false);
    }
    /* Pt + (%5 *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(60)){
      addEdge(other.from, base.to, 57, base, other, false);
    }
    /* Pt + (%6 *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(62)){
      addEdge(other.from, base.to, 57, base, other, false);
    }
    /* _Pt + (Sink2Obj *) => %7 */
    for(Edge other : base.to.getInEdges(58)){
      addEdge(other.from, base.from, 64, base, other, false);
    }
    /* Pt + (%8 *) => Src2ObjX */
    for(Edge other : base.from.getInEdges(70)){
      addEdge(other.from, base.to, 69, base, other, false);
    }
    /* Pt + (%9 *) => Src2ObjX */
    for(Edge other : base.from.getInEdges(71)){
      addEdge(other.from, base.to, 69, base, other, false);
    }
    /* Pt + (%10 *) => Src2ObjX */
    for(Edge other : base.from.getInEdges(73)){
      addEdge(other.from, base.to, 69, base, other, false);
    }
    /* Pt + (%11 *) => Sink2ObjX */
    for(Edge other : base.from.getInEdges(75)){
      addEdge(other.from, base.to, 74, base, other, false);
    }
    /* Pt + (%12 *) => Sink2ObjX */
    for(Edge other : base.from.getInEdges(76)){
      addEdge(other.from, base.to, 74, base, other, false);
    }
    /* Pt + (%13 *) => Sink2ObjX */
    for(Edge other : base.from.getInEdges(78)){
      addEdge(other.from, base.to, 74, base, other, false);
    }
    /* _Pt + (Src2ObjT *) => %14 */
    for(Edge other : base.to.getInEdges(55)){
      addEdge(other.from, base.from, 82, base, other, false);
    }
    /* _Pt + (Src2ObjX *) => %15 */
    for(Edge other : base.to.getInEdges(69)){
      addEdge(other.from, base.from, 84, base, other, false);
    }
    /* _Pt + (Src2PrimFld[i] *) => %16[i] */
    for(Edge other : base.to.getInEdges(68)){
      addEdge(other.from, base.from, 85, base, other, true);
    }
    /* Pt + (%17[i] *) => Src2PrimFld[i] */
    for(Edge other : base.from.getInEdges(89)){
      addEdge(other.from, base.to, 68, base, other, true);
    }
    /* Pt + (%18 *) => Src2PrimFldArr */
    for(Edge other : base.from.getInEdges(91)){
      addEdge(other.from, base.to, 72, base, other, false);
    }
    /* _Pt + (Sink2ObjT *) => %19 */
    for(Edge other : base.to.getInEdges(56)){
      addEdge(other.from, base.from, 93, base, other, false);
    }
    /* _Pt + (Sink2ObjX *) => %20 */
    for(Edge other : base.to.getInEdges(74)){
      addEdge(other.from, base.from, 94, base, other, false);
    }
    /* _Pt + (Sink2PrimFld[i] *) => %21[i] */
    for(Edge other : base.to.getInEdges(95)){
      addEdge(other.from, base.from, 96, base, other, true);
    }
    /* Pt + (%22[i] *) => Sink2PrimFld[i] */
    for(Edge other : base.from.getInEdges(98)){
      addEdge(other.from, base.to, 95, base, other, true);
    }
    /* Pt + (%23 *) => Sink2PrimFldArr */
    for(Edge other : base.from.getInEdges(99)){
      addEdge(other.from, base.to, 77, base, other, false);
    }
    /* _Pt + (Src2Obj *) => LabelRef3 */
    for(Edge other : base.to.getInEdges(66)){
      addEdge(other.from, base.from, 100, base, other, false);
    }
    /* _Pt + (Sink2Obj *) => LabelRef3 */
    for(Edge other : base.to.getInEdges(58)){
      addEdge(other.from, base.from, 100, base, other, false);
    }
    break;
  case 43: /* %1 */
    /* %1 + (* _PreFlowsTo) => Pt */
    for(Edge other : base.to.getInEdges(34)){
      addEdge(base.from, other.from, 42, base, other, false);
    }
    break;
  case 44: /* Fpt */
    /* Fpt + (Src2ObjT *) => Src2ObjT */
    for(Edge other : base.from.getInEdges(55)){
      addEdge(other.from, base.to, 55, base, other, false);
    }
    /* Fpt + (Sink2ObjT *) => Sink2ObjT */
    for(Edge other : base.from.getInEdges(56)){
      addEdge(other.from, base.to, 56, base, other, false);
    }
    /* Fpt + (SinkF2Obj *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(57)){
      addEdge(other.from, base.to, 57, base, other, false);
    }
    break;
  case 45: /* Store */
    /* Store[i] + (_Pt *) => %2[i] */
    for(Edge other : base.from.getOutEdges(42)){
      addEdge(other.to, base.to, 46, base, other, true);
    }
    break;
  case 46: /* %2 */
    /* %2[i] + (* Pt) => Fpt[i] */
    for(Edge other : base.to.getOutEdges(42)){
      addEdge(base.from, other.to, 44, base, other, true);
    }
    break;
  case 47: /* FptArr */
    /* _FptArr + (* Obj2RefT) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(53)){
      addEdge(base.to, other.to, 53, base, other, false);
    }
    /* _FptArr + (* Obj2PrimT) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(54)){
      addEdge(base.to, other.to, 54, base, other, false);
    }
    /* FptArr + (Src2ObjX *) => Src2ObjX */
    for(Edge other : base.from.getInEdges(69)){
      addEdge(other.from, base.to, 69, base, other, false);
    }
    /* FptArr + (Sink2ObjX *) => Sink2ObjX */
    for(Edge other : base.from.getInEdges(74)){
      addEdge(other.from, base.to, 74, base, other, false);
    }
    break;
  case 48: /* StoreArr */
    /* StoreArr + (_Pt *) => %3 */
    for(Edge other : base.from.getOutEdges(42)){
      addEdge(other.to, base.to, 49, base, other, false);
    }
    break;
  case 49: /* %3 */
    /* %3 + (* Pt) => FptArr */
    for(Edge other : base.to.getOutEdges(42)){
      addEdge(base.from, other.to, 47, base, other, false);
    }
    break;
  case 50: /* pt */
    /* pt => Pt */
    addEdge(base.from, base.to, 42, base, false);
    break;
  case 51: /* fpt */
    /* fpt[i] => Fpt[i] */
    addEdge(base.from, base.to, 44, base, true);
    break;
  case 52: /* fptArr */
    /* fptArr => FptArr */
    addEdge(base.from, base.to, 47, base, false);
    break;
  case 53: /* Obj2RefT */
    /* Obj2RefT + (_FptArr *) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(47)){
      addEdge(other.to, base.to, 53, base, other, false);
    }
    /* Obj2RefT + (Src2Obj *) => %8 */
    for(Edge other : base.from.getInEdges(66)){
      addEdge(other.from, base.to, 70, base, other, false);
    }
    /* Obj2RefT + (Src2PrimFldArr *) => %10 */
    for(Edge other : base.from.getInEdges(72)){
      addEdge(other.from, base.to, 73, base, other, false);
    }
    /* Obj2RefT + (Sink2Obj *) => %11 */
    for(Edge other : base.from.getInEdges(58)){
      addEdge(other.from, base.to, 75, base, other, false);
    }
    /* Obj2RefT + (Sink2PrimFldArr *) => %13 */
    for(Edge other : base.from.getInEdges(77)){
      addEdge(other.from, base.to, 78, base, other, false);
    }
    break;
  case 54: /* Obj2PrimT */
    /* Obj2PrimT + (_FptArr *) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(47)){
      addEdge(other.to, base.to, 54, base, other, false);
    }
    /* Obj2PrimT + (Src2Obj *) => Src2Prim */
    for(Edge other : base.from.getInEdges(66)){
      addEdge(other.from, base.to, 67, base, other, false);
    }
    /* Obj2PrimT + (Src2PrimFldArr *) => Src2Prim */
    for(Edge other : base.from.getInEdges(72)){
      addEdge(other.from, base.to, 67, base, other, false);
    }
    /* Obj2PrimT + (Sink2Obj *) => Sink2Prim */
    for(Edge other : base.from.getInEdges(58)){
      addEdge(other.from, base.to, 61, base, other, false);
    }
    /* Obj2PrimT + (Sink2PrimFldArr *) => Sink2Prim */
    for(Edge other : base.from.getInEdges(77)){
      addEdge(other.from, base.to, 61, base, other, false);
    }
    break;
  case 55: /* Src2ObjT */
    /* Src2ObjT + (* Fpt) => Src2ObjT */
    for(Edge other : base.to.getOutEdges(44)){
      addEdge(base.from, other.to, 55, base, other, false);
    }
    /* Src2ObjT => Src2Obj */
    addEdge(base.from, base.to, 66, base, false);
    /* Src2ObjT + (* _Pt) => %14 */
    for(Edge other : base.to.getInEdges(42)){
      addEdge(base.from, other.from, 82, base, other, false);
    }
    break;
  case 56: /* Sink2ObjT */
    /* Sink2ObjT + (* Fpt) => Sink2ObjT */
    for(Edge other : base.to.getOutEdges(44)){
      addEdge(base.from, other.to, 56, base, other, false);
    }
    /* Sink2ObjT => Sink2Obj */
    addEdge(base.from, base.to, 58, base, false);
    /* Sink2ObjT + (* _Pt) => %19 */
    for(Edge other : base.to.getInEdges(42)){
      addEdge(base.from, other.from, 93, base, other, false);
    }
    break;
  case 57: /* SinkF2Obj */
    /* SinkF2Obj + (* Fpt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(44)){
      addEdge(base.from, other.to, 57, base, other, false);
    }
    /* _SinkF2Obj + (Src2Obj *) => Src2Sink */
    for(Edge other : base.to.getInEdges(66)){
      addEdge(other.from, base.from, 65, base, other, false);
    }
    /* _SinkF2Obj + (Src2PrimFld *) => Src2Sink */
    for(Edge other : base.to.getInEdges(68)){
      addEdge(other.from, base.from, 65, base, other, false);
    }
    break;
  case 58: /* Sink2Obj */
    /* Sink2Obj + (* _Pt) => %4 */
    for(Edge other : base.to.getInEdges(42)){
      addEdge(base.from, other.from, 59, base, other, false);
    }
    /* Sink2Obj + (* _Pt) => %7 */
    for(Edge other : base.to.getInEdges(42)){
      addEdge(base.from, other.from, 64, base, other, false);
    }
    /* Sink2Obj + (* Obj2RefT) => %11 */
    for(Edge other : base.to.getOutEdges(53)){
      addEdge(base.from, other.to, 75, base, other, false);
    }
    /* Sink2Obj + (* Obj2PrimT) => Sink2Prim */
    for(Edge other : base.to.getOutEdges(54)){
      addEdge(base.from, other.to, 61, base, other, false);
    }
    /* Sink2Obj + (* _Pt) => LabelRef3 */
    for(Edge other : base.to.getInEdges(42)){
      addEdge(base.from, other.from, 100, base, other, false);
    }
    break;
  case 59: /* %4 */
    /* %4 + (* _Ref2RefF) => %5 */
    for(Edge other : base.to.getInEdges(20)){
      addEdge(base.from, other.from, 60, base, other, false);
    }
    break;
  case 60: /* %5 */
    /* %5 + (* Pt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(42)){
      addEdge(base.from, other.to, 57, base, other, false);
    }
    break;
  case 61: /* Sink2Prim */
    /* Sink2Prim + (* _Ref2PrimF) => %6 */
    for(Edge other : base.to.getInEdges(22)){
      addEdge(base.from, other.from, 62, base, other, false);
    }
    /* Sink2Prim + (* _Prim2PrimF) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(26)){
      addEdge(base.from, other.from, 63, base, other, false);
    }
    /* Sink2Prim + (* Prim2RefT) => %12 */
    for(Edge other : base.to.getOutEdges(4)){
      addEdge(base.from, other.to, 76, base, other, false);
    }
    /* Sink2Prim + (* _assignPrimCtxt) => Sink2Prim */
    for(Edge other : base.to.getInEdges(79)){
      addEdge(base.from, other.from, 61, base, other, false);
    }
    /* Sink2Prim + (* _assignPrimCCtxt) => Sink2Prim */
    for(Edge other : base.to.getInEdges(80)){
      addEdge(base.from, other.from, 61, base, other, false);
    }
    /* Sink2Prim + (* Prim2PrimT) => Sink2Prim */
    for(Edge other : base.to.getOutEdges(6)){
      addEdge(base.from, other.to, 61, base, other, false);
    }
    /* Sink2Prim + (* _storePrimCtxt[i]) => %22[i] */
    for(Edge other : base.to.getInEdges(88)){
      addEdge(base.from, other.from, 98, base, other, true);
    }
    /* Sink2Prim + (* _storePrimCtxtArr) => %23 */
    for(Edge other : base.to.getInEdges(90)){
      addEdge(base.from, other.from, 99, base, other, false);
    }
    /* Sink2Prim + (* _storeStatPrimCtxt[i]) => Sink2PrimFldStat[i] */
    for(Edge other : base.to.getInEdges(92)){
      addEdge(base.from, other.from, 97, base, other, true);
    }
    /* Sink2Prim => LabelPrim3 */
    addEdge(base.from, base.to, 101, base, false);
    break;
  case 62: /* %6 */
    /* %6 + (* Pt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(42)){
      addEdge(base.from, other.to, 57, base, other, false);
    }
    break;
  case 63: /* SinkF2Prim */
    /* _SinkF2Prim + (Src2Prim *) => Src2Sink */
    for(Edge other : base.to.getInEdges(67)){
      addEdge(other.from, base.from, 65, base, other, false);
    }
    break;
  case 64: /* %7 */
    /* %7 + (* _Prim2RefF) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(24)){
      addEdge(base.from, other.from, 63, base, other, false);
    }
    break;
  case 65: /* Src2Sink */
    /* Src2Sink => Flow3 */
    addEdge(base.from, base.to, 102, base, false);
    break;
  case 66: /* Src2Obj */
    /* Src2Obj + (* _SinkF2Obj) => Src2Sink */
    for(Edge other : base.to.getInEdges(57)){
      addEdge(base.from, other.from, 65, base, other, false);
    }
    /* Src2Obj + (* Obj2RefT) => %8 */
    for(Edge other : base.to.getOutEdges(53)){
      addEdge(base.from, other.to, 70, base, other, false);
    }
    /* Src2Obj + (* Obj2PrimT) => Src2Prim */
    for(Edge other : base.to.getOutEdges(54)){
      addEdge(base.from, other.to, 67, base, other, false);
    }
    /* Src2Obj + (* _Pt) => LabelRef3 */
    for(Edge other : base.to.getInEdges(42)){
      addEdge(base.from, other.from, 100, base, other, false);
    }
    break;
  case 67: /* Src2Prim */
    /* Src2Prim + (* _SinkF2Prim) => Src2Sink */
    for(Edge other : base.to.getInEdges(63)){
      addEdge(base.from, other.from, 65, base, other, false);
    }
    /* Src2Prim + (* Prim2RefT) => %9 */
    for(Edge other : base.to.getOutEdges(4)){
      addEdge(base.from, other.to, 71, base, other, false);
    }
    /* Src2Prim + (* _assignPrimCtxt) => Src2Prim */
    for(Edge other : base.to.getInEdges(79)){
      addEdge(base.from, other.from, 67, base, other, false);
    }
    /* Src2Prim + (* _assignPrimCCtxt) => Src2Prim */
    for(Edge other : base.to.getInEdges(80)){
      addEdge(base.from, other.from, 67, base, other, false);
    }
    /* Src2Prim + (* Prim2PrimT) => Src2Prim */
    for(Edge other : base.to.getOutEdges(6)){
      addEdge(base.from, other.to, 67, base, other, false);
    }
    /* Src2Prim + (* _storePrimCtxt[i]) => %17[i] */
    for(Edge other : base.to.getInEdges(88)){
      addEdge(base.from, other.from, 89, base, other, true);
    }
    /* Src2Prim + (* _storePrimCtxtArr) => %18 */
    for(Edge other : base.to.getInEdges(90)){
      addEdge(base.from, other.from, 91, base, other, false);
    }
    /* Src2Prim + (* _storeStatPrimCtxt[i]) => Src2PrimFldStat[i] */
    for(Edge other : base.to.getInEdges(92)){
      addEdge(base.from, other.from, 86, base, other, true);
    }
    /* Src2Prim => LabelPrim3 */
    addEdge(base.from, base.to, 101, base, false);
    break;
  case 68: /* Src2PrimFld */
    /* Src2PrimFld + (* _SinkF2Obj) => Src2Sink */
    for(Edge other : base.to.getInEdges(57)){
      addEdge(base.from, other.from, 65, base, other, false);
    }
    /* Src2PrimFld[i] + (* _Pt) => %16[i] */
    for(Edge other : base.to.getInEdges(42)){
      addEdge(base.from, other.from, 85, base, other, true);
    }
    break;
  case 69: /* Src2ObjX */
    /* Src2ObjX => Src2Obj */
    addEdge(base.from, base.to, 66, base, false);
    /* Src2ObjX + (* FptArr) => Src2ObjX */
    for(Edge other : base.to.getOutEdges(47)){
      addEdge(base.from, other.to, 69, base, other, false);
    }
    /* Src2ObjX + (* _Pt) => %15 */
    for(Edge other : base.to.getInEdges(42)){
      addEdge(base.from, other.from, 84, base, other, false);
    }
    break;
  case 70: /* %8 */
    /* %8 + (* Pt) => Src2ObjX */
    for(Edge other : base.to.getOutEdges(42)){
      addEdge(base.from, other.to, 69, base, other, false);
    }
    break;
  case 71: /* %9 */
    /* %9 + (* Pt) => Src2ObjX */
    for(Edge other : base.to.getOutEdges(42)){
      addEdge(base.from, other.to, 69, base, other, false);
    }
    break;
  case 72: /* Src2PrimFldArr */
    /* Src2PrimFldArr + (* Obj2RefT) => %10 */
    for(Edge other : base.to.getOutEdges(53)){
      addEdge(base.from, other.to, 73, base, other, false);
    }
    /* Src2PrimFldArr + (* Obj2PrimT) => Src2Prim */
    for(Edge other : base.to.getOutEdges(54)){
      addEdge(base.from, other.to, 67, base, other, false);
    }
    break;
  case 73: /* %10 */
    /* %10 + (* Pt) => Src2ObjX */
    for(Edge other : base.to.getOutEdges(42)){
      addEdge(base.from, other.to, 69, base, other, false);
    }
    break;
  case 74: /* Sink2ObjX */
    /* Sink2ObjX => Sink2Obj */
    addEdge(base.from, base.to, 58, base, false);
    /* Sink2ObjX + (* FptArr) => Sink2ObjX */
    for(Edge other : base.to.getOutEdges(47)){
      addEdge(base.from, other.to, 74, base, other, false);
    }
    /* Sink2ObjX + (* _Pt) => %20 */
    for(Edge other : base.to.getInEdges(42)){
      addEdge(base.from, other.from, 94, base, other, false);
    }
    break;
  case 75: /* %11 */
    /* %11 + (* Pt) => Sink2ObjX */
    for(Edge other : base.to.getOutEdges(42)){
      addEdge(base.from, other.to, 74, base, other, false);
    }
    break;
  case 76: /* %12 */
    /* %12 + (* Pt) => Sink2ObjX */
    for(Edge other : base.to.getOutEdges(42)){
      addEdge(base.from, other.to, 74, base, other, false);
    }
    break;
  case 77: /* Sink2PrimFldArr */
    /* Sink2PrimFldArr + (* Obj2RefT) => %13 */
    for(Edge other : base.to.getOutEdges(53)){
      addEdge(base.from, other.to, 78, base, other, false);
    }
    /* Sink2PrimFldArr + (* Obj2PrimT) => Sink2Prim */
    for(Edge other : base.to.getOutEdges(54)){
      addEdge(base.from, other.to, 61, base, other, false);
    }
    break;
  case 78: /* %13 */
    /* %13 + (* Pt) => Sink2ObjX */
    for(Edge other : base.to.getOutEdges(42)){
      addEdge(base.from, other.to, 74, base, other, false);
    }
    break;
  case 79: /* assignPrimCtxt */
    /* _assignPrimCtxt + (Src2Prim *) => Src2Prim */
    for(Edge other : base.to.getInEdges(67)){
      addEdge(other.from, base.from, 67, base, other, false);
    }
    /* _assignPrimCtxt + (Sink2Prim *) => Sink2Prim */
    for(Edge other : base.to.getInEdges(61)){
      addEdge(other.from, base.from, 61, base, other, false);
    }
    break;
  case 80: /* assignPrimCCtxt */
    /* _assignPrimCCtxt + (Src2Prim *) => Src2Prim */
    for(Edge other : base.to.getInEdges(67)){
      addEdge(other.from, base.from, 67, base, other, false);
    }
    /* _assignPrimCCtxt + (Sink2Prim *) => Sink2Prim */
    for(Edge other : base.to.getInEdges(61)){
      addEdge(other.from, base.from, 61, base, other, false);
    }
    break;
  case 81: /* loadPrimCtxt */
    /* _loadPrimCtxt + (%14 *) => Src2Prim */
    for(Edge other : base.to.getInEdges(82)){
      addEdge(other.from, base.from, 67, base, other, false);
    }
    /* _loadPrimCtxt[i] + (%16[i] *) => Src2Prim */
    for(Edge other : base.to.getInEdges(85)){
      addEdge(other.from, base.from, 67, base, other, false);
    }
    /* _loadPrimCtxt + (%19 *) => Sink2Prim */
    for(Edge other : base.to.getInEdges(93)){
      addEdge(other.from, base.from, 61, base, other, false);
    }
    /* _loadPrimCtxt[i] + (%21[i] *) => Sink2Prim */
    for(Edge other : base.to.getInEdges(96)){
      addEdge(other.from, base.from, 61, base, other, false);
    }
    break;
  case 82: /* %14 */
    /* %14 + (* _loadPrimCtxt) => Src2Prim */
    for(Edge other : base.to.getInEdges(81)){
      addEdge(base.from, other.from, 67, base, other, false);
    }
    break;
  case 83: /* loadPrimCtxtArr */
    /* _loadPrimCtxtArr + (%15 *) => Src2Prim */
    for(Edge other : base.to.getInEdges(84)){
      addEdge(other.from, base.from, 67, base, other, false);
    }
    /* _loadPrimCtxtArr + (%20 *) => Sink2Prim */
    for(Edge other : base.to.getInEdges(94)){
      addEdge(other.from, base.from, 61, base, other, false);
    }
    break;
  case 84: /* %15 */
    /* %15 + (* _loadPrimCtxtArr) => Src2Prim */
    for(Edge other : base.to.getInEdges(83)){
      addEdge(base.from, other.from, 67, base, other, false);
    }
    break;
  case 85: /* %16 */
    /* %16[i] + (* _loadPrimCtxt[i]) => Src2Prim */
    for(Edge other : base.to.getInEdges(81)){
      addEdge(base.from, other.from, 67, base, other, false);
    }
    break;
  case 86: /* Src2PrimFldStat */
    /* Src2PrimFldStat[i] + (* _loadStatPrimCtxt[i]) => Src2Prim */
    for(Edge other : base.to.getInEdges(87)){
      addEdge(base.from, other.from, 67, base, other, false);
    }
    break;
  case 87: /* loadStatPrimCtxt */
    /* _loadStatPrimCtxt[i] + (Src2PrimFldStat[i] *) => Src2Prim */
    for(Edge other : base.to.getInEdges(86)){
      addEdge(other.from, base.from, 67, base, other, false);
    }
    /* _loadStatPrimCtxt[i] + (Sink2PrimFldStat[i] *) => Sink2Prim */
    for(Edge other : base.to.getInEdges(97)){
      addEdge(other.from, base.from, 61, base, other, false);
    }
    break;
  case 88: /* storePrimCtxt */
    /* _storePrimCtxt[i] + (Src2Prim *) => %17[i] */
    for(Edge other : base.to.getInEdges(67)){
      addEdge(other.from, base.from, 89, base, other, true);
    }
    /* _storePrimCtxt[i] + (Sink2Prim *) => %22[i] */
    for(Edge other : base.to.getInEdges(61)){
      addEdge(other.from, base.from, 98, base, other, true);
    }
    break;
  case 89: /* %17 */
    /* %17[i] + (* Pt) => Src2PrimFld[i] */
    for(Edge other : base.to.getOutEdges(42)){
      addEdge(base.from, other.to, 68, base, other, true);
    }
    break;
  case 90: /* storePrimCtxtArr */
    /* _storePrimCtxtArr + (Src2Prim *) => %18 */
    for(Edge other : base.to.getInEdges(67)){
      addEdge(other.from, base.from, 91, base, other, false);
    }
    /* _storePrimCtxtArr + (Sink2Prim *) => %23 */
    for(Edge other : base.to.getInEdges(61)){
      addEdge(other.from, base.from, 99, base, other, false);
    }
    break;
  case 91: /* %18 */
    /* %18 + (* Pt) => Src2PrimFldArr */
    for(Edge other : base.to.getOutEdges(42)){
      addEdge(base.from, other.to, 72, base, other, false);
    }
    break;
  case 92: /* storeStatPrimCtxt */
    /* _storeStatPrimCtxt[i] + (Src2Prim *) => Src2PrimFldStat[i] */
    for(Edge other : base.to.getInEdges(67)){
      addEdge(other.from, base.from, 86, base, other, true);
    }
    /* _storeStatPrimCtxt[i] + (Sink2Prim *) => Sink2PrimFldStat[i] */
    for(Edge other : base.to.getInEdges(61)){
      addEdge(other.from, base.from, 97, base, other, true);
    }
    break;
  case 93: /* %19 */
    /* %19 + (* _loadPrimCtxt) => Sink2Prim */
    for(Edge other : base.to.getInEdges(81)){
      addEdge(base.from, other.from, 61, base, other, false);
    }
    break;
  case 94: /* %20 */
    /* %20 + (* _loadPrimCtxtArr) => Sink2Prim */
    for(Edge other : base.to.getInEdges(83)){
      addEdge(base.from, other.from, 61, base, other, false);
    }
    break;
  case 95: /* Sink2PrimFld */
    /* Sink2PrimFld[i] + (* _Pt) => %21[i] */
    for(Edge other : base.to.getInEdges(42)){
      addEdge(base.from, other.from, 96, base, other, true);
    }
    break;
  case 96: /* %21 */
    /* %21[i] + (* _loadPrimCtxt[i]) => Sink2Prim */
    for(Edge other : base.to.getInEdges(81)){
      addEdge(base.from, other.from, 61, base, other, false);
    }
    break;
  case 97: /* Sink2PrimFldStat */
    /* Sink2PrimFldStat[i] + (* _loadStatPrimCtxt[i]) => Sink2Prim */
    for(Edge other : base.to.getInEdges(87)){
      addEdge(base.from, other.from, 61, base, other, false);
    }
    break;
  case 98: /* %22 */
    /* %22[i] + (* Pt) => Sink2PrimFld[i] */
    for(Edge other : base.to.getOutEdges(42)){
      addEdge(base.from, other.to, 95, base, other, true);
    }
    break;
  case 99: /* %23 */
    /* %23 + (* Pt) => Sink2PrimFldArr */
    for(Edge other : base.to.getOutEdges(42)){
      addEdge(base.from, other.to, 77, base, other, false);
    }
    break;
  }
}

public String[] outputRels() {
    String[] rels = {"LabelRef3", "LabelPrim3", "Flow3"};
    return rels;
}

public short kindToWeight(int kind) {
  switch (kind) {
  case 7:
    return (short)1;
  case 28:
    return (short)1;
  case 29:
    return (short)1;
  case 30:
    return (short)1;
  case 31:
    return (short)1;
  case 32:
    return (short)1;
  case 40:
    return (short)1;
  default:
    return (short)0;
  }
}

public boolean useReps() { return false; }

}