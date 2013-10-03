package stamp.missingmodels.grammars;
import stamp.missingmodels.util.jcflsolver.*;

/* Original Grammar:
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
# RULES: OBJECT ANNOTATIONS
###################

Obj2RefT :: _pt Ref2RefT
Obj2PrimT :: _pt Ref2PrimT
Obj2RefT :: _fptArr Obj2RefT
Obj2PrimT :: _fptArr Obj2PrimT

Src2ObjT :: Src2RefT pt
Src2ObjT :: Src2ObjT fpt[*]

Sink2ObjT :: Sink2RefT pt
Sink2ObjT :: Sink2ObjT fpt[*]

###################
# RULES: SINKF
###################

# Sink_full-obj flow

SinkF2Obj :: SinkF2RefF pt
SinkF2Obj :: Sink2Obj _pt _Ref2RefF pt
SinkF2Obj :: Sink2Prim _Ref2PrimF pt
SinkF2Obj :: SinkF2Obj fpt[*]

# Sink_full-prim flow

SinkF2Prim :: SinkF2PrimF
SinkF2Prim :: Sink2Obj _pt _Prim2RefF
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

Src2ObjX :: Src2Obj Obj2RefT pt
Src2ObjX :: Src2Prim Prim2RefT pt
Src2ObjX :: Src2PrimFldArr Obj2RefT pt
Src2ObjX :: Src2ObjX fptArr

# Sink-obj flow

Sink2Obj :: Sink2ObjT
Sink2Obj :: Sink2ObjX

Sink2ObjX :: Sink2Obj Obj2RefT pt
Sink2ObjX :: Sink2Prim Prim2RefT pt
Sink2ObjX :: Sink2PrimFldArr Obj2RefT pt
Sink2ObjX :: Sink2ObjX fptArr

# Src-prim flow

Src2Prim :: Src2PrimT
Src2Prim :: Src2Prim _assignPrimCtxt
Src2Prim :: Src2Prim _assignPrimCCtxt

Src2Prim :: Src2Obj Obj2PrimT
Src2Prim :: Src2Prim Prim2PrimT

Src2Prim :: Src2ObjT _pt _loadPrimCtxt[*]
Src2Prim :: Src2ObjX _pt _loadPrimCtxtArr
Src2Prim :: Src2PrimFldArr Obj2PrimT

# cl Src2PrimFld[f] o _pt v_c _loadPrimCtxt[f] u_c
Src2Prim :: Src2PrimFld[f] _pt _loadPrimCtxt[f]
Src2Prim :: Src2PrimFldStat[f] _loadStatPrimCtxt[f]

# Src-prim_fld flow

Src2PrimFld[f] :: Src2Prim _storePrimCtxt[f] pt
Src2PrimFldArr :: Src2Prim _storePrimCtxtArr pt
Src2PrimFldStat[f] :: Src2Prim _storeStatPrimCtxt[f]

# Sink-prim flow

Sink2Prim :: Sink2PrimT
Sink2Prim :: Sink2Prim _assignPrimCtxt
Sink2Prim :: Sink2Prim _assignPrimCCtxt

Sink2Prim :: Sink2Obj Obj2PrimT
Sink2Prim :: Sink2Prim Prim2PrimT

Sink2Prim :: Sink2ObjT _pt _loadPrimCtxt[*]
Sink2Prim :: Sink2ObjX _pt _loadPrimCtxtArr
Sink2Prim :: Sink2PrimFldArr Obj2PrimT

# cl Sink2PrimFld[f] o _pt v_c _loadPrimCtxt[f] u_c
Sink2Prim :: Sink2PrimFld[f] _pt _loadPrimCtxt[f]
Sink2Prim :: Sink2PrimFldStat[f] _loadStatPrimCtxt[f]

# Sink-prim_fld flow

Sink2PrimFld[f] :: Sink2Prim _storePrimCtxt[f] pt
Sink2PrimFldArr :: Sink2Prim _storePrimCtxtArr pt
Sink2PrimFldStat[f] :: Sink2Prim _storeStatPrimCtxt[f]

###################
# RULES: OUTPUT
###################

LabelRef3 :: Src2Obj _pt
LabelRef3 :: Sink2Obj _pt
LabelPrim3 :: Src2Prim
LabelPrim3 :: Sink2Prim
Flow3 :: Src2Sink
*/

/* Normalized Grammar:
%13:
	%13[i] :: Src2Prim _storePrimCtxt[i]
SinkF2Obj:
	SinkF2Obj :: SinkF2RefF pt
	SinkF2Obj :: %1 pt
	SinkF2Obj :: %2 pt
	SinkF2Obj :: SinkF2Obj fpt
Sink2RefT:
	Sink2RefT :: sink2RefT
Sink2PrimFldStat:
	Sink2PrimFldStat[i] :: Sink2Prim _storeStatPrimCtxt[i]
%9:
	%9 :: Sink2PrimFldArr Obj2RefT
%8:
	%8 :: Sink2Prim Prim2RefT
%4:
	%4 :: Src2Obj Obj2RefT
%5:
	%5 :: Src2Prim Prim2RefT
Src2PrimT:
	Src2PrimT :: src2PrimT
%7:
	%7 :: Sink2Obj Obj2RefT
%6:
	%6 :: Src2PrimFldArr Obj2RefT
%1:
	%1 :: %0 _Ref2RefF
%0:
	%0 :: Sink2Obj _pt
%3:
	%3 :: Sink2Obj _pt
%2:
	%2 :: Sink2Prim _Ref2PrimF
Sink2PrimT:
	Sink2PrimT :: sink2PrimT
Src2PrimFldStat:
	Src2PrimFldStat[i] :: Src2Prim _storeStatPrimCtxt[i]
Sink2Prim:
	Sink2Prim :: Sink2PrimT
	Sink2Prim :: Sink2Prim _assignPrimCtxt
	Sink2Prim :: Sink2Prim _assignPrimCCtxt
	Sink2Prim :: Sink2Obj Obj2PrimT
	Sink2Prim :: Sink2Prim Prim2PrimT
	Sink2Prim :: %15 _loadPrimCtxt
	Sink2Prim :: %16 _loadPrimCtxtArr
	Sink2Prim :: Sink2PrimFldArr Obj2PrimT
	Sink2Prim :: %17[i] _loadPrimCtxt[i]
	Sink2Prim :: Sink2PrimFldStat[i] _loadStatPrimCtxt[i]
Sink2PrimFldArr:
	Sink2PrimFldArr :: %19 pt
SinkF2PrimF:
	SinkF2PrimF :: sinkF2PrimF
Sink2PrimFld:
	Sink2PrimFld[i] :: %18[i] pt
%19:
	%19 :: Sink2Prim _storePrimCtxtArr
Src2Sink:
	Src2Sink :: Src2Obj _SinkF2Obj
	Src2Sink :: Src2Prim _SinkF2Prim
	Src2Sink :: Src2PrimFld _SinkF2Obj
Src2PrimFldArr:
	Src2PrimFldArr :: %14 pt
Src2Prim:
	Src2Prim :: Src2PrimT
	Src2Prim :: Src2Prim _assignPrimCtxt
	Src2Prim :: Src2Prim _assignPrimCCtxt
	Src2Prim :: Src2Obj Obj2PrimT
	Src2Prim :: Src2Prim Prim2PrimT
	Src2Prim :: %10 _loadPrimCtxt
	Src2Prim :: %11 _loadPrimCtxtArr
	Src2Prim :: Src2PrimFldArr Obj2PrimT
	Src2Prim :: %12[i] _loadPrimCtxt[i]
	Src2Prim :: Src2PrimFldStat[i] _loadStatPrimCtxt[i]
Prim2PrimF:
	Prim2PrimF :: prim2PrimF
Sink2Obj:
	Sink2Obj :: Sink2ObjT
	Sink2Obj :: Sink2ObjX
Src2PrimFld:
	Src2PrimFld[i] :: %13[i] pt
LabelPrim3:
	LabelPrim3 :: Src2Prim
	LabelPrim3 :: Sink2Prim
SinkF2RefF:
	SinkF2RefF :: sinkF2RefF
Sink2ObjT:
	Sink2ObjT :: Sink2RefT pt
	Sink2ObjT :: Sink2ObjT fpt
Obj2PrimT:
	Obj2PrimT :: _pt Ref2PrimT
	Obj2PrimT :: _fptArr Obj2PrimT
Sink2ObjX:
	Sink2ObjX :: %7 pt
	Sink2ObjX :: %8 pt
	Sink2ObjX :: %9 pt
	Sink2ObjX :: Sink2ObjX fptArr
%14:
	%14 :: Src2Prim _storePrimCtxtArr
Prim2PrimT:
	Prim2PrimT :: prim2PrimT
	Prim2PrimT :: primArg2PrimRetTStub
Obj2RefT:
	Obj2RefT :: _pt Ref2RefT
	Obj2RefT :: _fptArr Obj2RefT
Ref2PrimT:
	Ref2PrimT :: ref2PrimT
	Ref2PrimT :: refArg2PrimRetTStub
Src2ObjX:
	Src2ObjX :: %4 pt
	Src2ObjX :: %5 pt
	Src2ObjX :: %6 pt
	Src2ObjX :: Src2ObjX fptArr
SinkF2Prim:
	SinkF2Prim :: SinkF2PrimF
	SinkF2Prim :: %3 _Prim2RefF
	SinkF2Prim :: Sink2Prim _Prim2PrimF
Ref2RefF:
	Ref2RefF :: ref2RefF
%11:
	%11 :: Src2ObjX _pt
%10:
	%10 :: Src2ObjT _pt
Prim2RefT:
	Prim2RefT :: prim2RefT
	Prim2RefT :: primArg2RefArgTStub
	Prim2RefT :: primArg2RefRetTStub
%12:
	%12[i] :: Src2PrimFld[i] _pt
%15:
	%15 :: Sink2ObjT _pt
Src2ObjT:
	Src2ObjT :: Src2RefT pt
	Src2ObjT :: Src2ObjT fpt
%17:
	%17[i] :: Sink2PrimFld[i] _pt
%16:
	%16 :: Sink2ObjX _pt
Ref2PrimF:
	Ref2PrimF :: ref2PrimF
%18:
	%18[i] :: Sink2Prim _storePrimCtxt[i]
Ref2RefT:
	Ref2RefT :: ref2RefT
	Ref2RefT :: refArg2RefArgTStub
	Ref2RefT :: refArg2RefRetTStub
Prim2RefF:
	Prim2RefF :: prim2RefF
Flow3:
	Flow3 :: Src2Sink
Src2Obj:
	Src2Obj :: Src2ObjT
	Src2Obj :: Src2ObjX
Src2RefT:
	Src2RefT :: src2RefT
LabelRef3:
	LabelRef3 :: Src2Obj _pt
	LabelRef3 :: Sink2Obj _pt
*/

/* Reverse Productions:
ref2PrimT:
	ref2PrimT => Ref2PrimT
%13:
	%13[i] + (* pt) => Src2PrimFld[i]
storeStatPrimCtxt:
	_storeStatPrimCtxt[i] + (Src2Prim *) => Src2PrimFldStat[i]
	_storeStatPrimCtxt[i] + (Sink2Prim *) => Sink2PrimFldStat[i]
storePrimCtxtArr:
	_storePrimCtxtArr + (Src2Prim *) => %14
	_storePrimCtxtArr + (Sink2Prim *) => %19
ref2RefF:
	ref2RefF => Ref2RefF
refArg2RefArgTStub:
	refArg2RefArgTStub => Ref2RefT
ref2RefT:
	ref2RefT => Ref2RefT
assignPrimCtxt:
	_assignPrimCtxt + (Src2Prim *) => Src2Prim
	_assignPrimCtxt + (Sink2Prim *) => Sink2Prim
prim2RefT:
	prim2RefT => Prim2RefT
SinkF2Obj:
	SinkF2Obj + (* fpt) => SinkF2Obj
	_SinkF2Obj + (Src2Obj *) => Src2Sink
	_SinkF2Obj + (Src2PrimFld *) => Src2Sink
primArg2RefRetTStub:
	primArg2RefRetTStub => Prim2RefT
ref2PrimF:
	ref2PrimF => Ref2PrimF
Sink2PrimFldStat:
	Sink2PrimFldStat[i] + (* _loadStatPrimCtxt[i]) => Sink2Prim
%9:
	%9 + (* pt) => Sink2ObjX
%8:
	%8 + (* pt) => Sink2ObjX
sinkF2PrimF:
	sinkF2PrimF => SinkF2PrimF
Sink2ObjX:
	Sink2ObjX => Sink2Obj
	Sink2ObjX + (* fptArr) => Sink2ObjX
	Sink2ObjX + (* _pt) => %16
prim2RefF:
	prim2RefF => Prim2RefF
%4:
	%4 + (* pt) => Src2ObjX
%7:
	%7 + (* pt) => Sink2ObjX
%6:
	%6 + (* pt) => Src2ObjX
%1:
	%1 + (* pt) => SinkF2Obj
primArg2PrimRetTStub:
	primArg2PrimRetTStub => Prim2PrimT
%3:
	%3 + (* _Prim2RefF) => SinkF2Prim
%2:
	%2 + (* pt) => SinkF2Obj
Src2PrimFldArr:
	Src2PrimFldArr + (* Obj2RefT) => %6
	Src2PrimFldArr + (* Obj2PrimT) => Src2Prim
Src2PrimFldStat:
	Src2PrimFldStat[i] + (* _loadStatPrimCtxt[i]) => Src2Prim
pt:
	_pt + (* Ref2RefT) => Obj2RefT
	_pt + (* Ref2PrimT) => Obj2PrimT
	pt + (Src2RefT *) => Src2ObjT
	pt + (Sink2RefT *) => Sink2ObjT
	pt + (SinkF2RefF *) => SinkF2Obj
	_pt + (Sink2Obj *) => %0
	pt + (%1 *) => SinkF2Obj
	pt + (%2 *) => SinkF2Obj
	_pt + (Sink2Obj *) => %3
	pt + (%4 *) => Src2ObjX
	pt + (%5 *) => Src2ObjX
	pt + (%6 *) => Src2ObjX
	pt + (%7 *) => Sink2ObjX
	pt + (%8 *) => Sink2ObjX
	pt + (%9 *) => Sink2ObjX
	_pt + (Src2ObjT *) => %10
	_pt + (Src2ObjX *) => %11
	_pt + (Src2PrimFld[i] *) => %12[i]
	pt + (%13[i] *) => Src2PrimFld[i]
	pt + (%14 *) => Src2PrimFldArr
	_pt + (Sink2ObjT *) => %15
	_pt + (Sink2ObjX *) => %16
	_pt + (Sink2PrimFld[i] *) => %17[i]
	pt + (%18[i] *) => Sink2PrimFld[i]
	pt + (%19 *) => Sink2PrimFldArr
	_pt + (Src2Obj *) => LabelRef3
	_pt + (Sink2Obj *) => LabelRef3
fpt:
	fpt + (Src2ObjT *) => Src2ObjT
	fpt + (Sink2ObjT *) => Sink2ObjT
	fpt + (SinkF2Obj *) => SinkF2Obj
Sink2Prim:
	Sink2Prim + (* _Ref2PrimF) => %2
	Sink2Prim + (* _Prim2PrimF) => SinkF2Prim
	Sink2Prim + (* Prim2RefT) => %8
	Sink2Prim + (* _assignPrimCtxt) => Sink2Prim
	Sink2Prim + (* _assignPrimCCtxt) => Sink2Prim
	Sink2Prim + (* Prim2PrimT) => Sink2Prim
	Sink2Prim + (* _storePrimCtxt[i]) => %18[i]
	Sink2Prim + (* _storePrimCtxtArr) => %19
	Sink2Prim + (* _storeStatPrimCtxt[i]) => Sink2PrimFldStat[i]
	Sink2Prim => LabelPrim3
%17:
	%17[i] + (* _loadPrimCtxt[i]) => Sink2Prim
Src2PrimT:
	Src2PrimT => Src2Prim
loadPrimCtxt:
	_loadPrimCtxt + (%10 *) => Src2Prim
	_loadPrimCtxt[i] + (%12[i] *) => Src2Prim
	_loadPrimCtxt + (%15 *) => Sink2Prim
	_loadPrimCtxt[i] + (%17[i] *) => Sink2Prim
src2RefT:
	src2RefT => Src2RefT
sink2RefT:
	sink2RefT => Sink2RefT
sinkF2RefF:
	sinkF2RefF => SinkF2RefF
SinkF2PrimF:
	SinkF2PrimF => SinkF2Prim
refArg2RefRetTStub:
	refArg2RefRetTStub => Ref2RefT
%0:
	%0 + (* _Ref2RefF) => %1
Sink2PrimFld:
	Sink2PrimFld[i] + (* _pt) => %17[i]
Sink2RefT:
	Sink2RefT + (* pt) => Sink2ObjT
sink2PrimT:
	sink2PrimT => Sink2PrimT
%19:
	%19 + (* pt) => Sink2PrimFldArr
storePrimCtxt:
	_storePrimCtxt[i] + (Src2Prim *) => %13[i]
	_storePrimCtxt[i] + (Sink2Prim *) => %18[i]
Src2Sink:
	Src2Sink => Flow3
Sink2PrimT:
	Sink2PrimT => Sink2Prim
loadStatPrimCtxt:
	_loadStatPrimCtxt[i] + (Src2PrimFldStat[i] *) => Src2Prim
	_loadStatPrimCtxt[i] + (Sink2PrimFldStat[i] *) => Sink2Prim
primArg2RefArgTStub:
	primArg2RefArgTStub => Prim2RefT
Sink2Obj:
	Sink2Obj + (* _pt) => %0
	Sink2Obj + (* _pt) => %3
	Sink2Obj + (* Obj2RefT) => %7
	Sink2Obj + (* Obj2PrimT) => Sink2Prim
	Sink2Obj + (* _pt) => LabelRef3
Src2PrimFld:
	Src2PrimFld + (* _SinkF2Obj) => Src2Sink
	Src2PrimFld[i] + (* _pt) => %12[i]
SinkF2RefF:
	SinkF2RefF + (* pt) => SinkF2Obj
Sink2ObjT:
	Sink2ObjT + (* fpt) => Sink2ObjT
	Sink2ObjT => Sink2Obj
	Sink2ObjT + (* _pt) => %15
src2PrimT:
	src2PrimT => Src2PrimT
Obj2PrimT:
	Obj2PrimT + (_fptArr *) => Obj2PrimT
	Obj2PrimT + (Src2Obj *) => Src2Prim
	Obj2PrimT + (Src2PrimFldArr *) => Src2Prim
	Obj2PrimT + (Sink2Obj *) => Sink2Prim
	Obj2PrimT + (Sink2PrimFldArr *) => Sink2Prim
Src2Prim:
	Src2Prim + (* _SinkF2Prim) => Src2Sink
	Src2Prim + (* Prim2RefT) => %5
	Src2Prim + (* _assignPrimCtxt) => Src2Prim
	Src2Prim + (* _assignPrimCCtxt) => Src2Prim
	Src2Prim + (* Prim2PrimT) => Src2Prim
	Src2Prim + (* _storePrimCtxt[i]) => %13[i]
	Src2Prim + (* _storePrimCtxtArr) => %14
	Src2Prim + (* _storeStatPrimCtxt[i]) => Src2PrimFldStat[i]
	Src2Prim => LabelPrim3
%14:
	%14 + (* pt) => Src2PrimFldArr
Prim2PrimT:
	Prim2PrimT + (Src2Prim *) => Src2Prim
	Prim2PrimT + (Sink2Prim *) => Sink2Prim
Sink2PrimFldArr:
	Sink2PrimFldArr + (* Obj2RefT) => %9
	Sink2PrimFldArr + (* Obj2PrimT) => Sink2Prim
Obj2RefT:
	Obj2RefT + (_fptArr *) => Obj2RefT
	Obj2RefT + (Src2Obj *) => %4
	Obj2RefT + (Src2PrimFldArr *) => %6
	Obj2RefT + (Sink2Obj *) => %7
	Obj2RefT + (Sink2PrimFldArr *) => %9
Ref2PrimT:
	Ref2PrimT + (_pt *) => Obj2PrimT
Src2ObjX:
	Src2ObjX => Src2Obj
	Src2ObjX + (* fptArr) => Src2ObjX
	Src2ObjX + (* _pt) => %11
SinkF2Prim:
	_SinkF2Prim + (Src2Prim *) => Src2Sink
Ref2RefF:
	_Ref2RefF + (%0 *) => %1
loadPrimCtxtArr:
	_loadPrimCtxtArr + (%11 *) => Src2Prim
	_loadPrimCtxtArr + (%16 *) => Sink2Prim
fptArr:
	_fptArr + (* Obj2RefT) => Obj2RefT
	_fptArr + (* Obj2PrimT) => Obj2PrimT
	fptArr + (Src2ObjX *) => Src2ObjX
	fptArr + (Sink2ObjX *) => Sink2ObjX
assignPrimCCtxt:
	_assignPrimCCtxt + (Src2Prim *) => Src2Prim
	_assignPrimCCtxt + (Sink2Prim *) => Sink2Prim
Src2Obj:
	Src2Obj + (* _SinkF2Obj) => Src2Sink
	Src2Obj + (* Obj2RefT) => %4
	Src2Obj + (* Obj2PrimT) => Src2Prim
	Src2Obj + (* _pt) => LabelRef3
Prim2RefT:
	Prim2RefT + (Src2Prim *) => %5
	Prim2RefT + (Sink2Prim *) => %8
%12:
	%12[i] + (* _loadPrimCtxt[i]) => Src2Prim
%15:
	%15 + (* _loadPrimCtxt) => Sink2Prim
Src2ObjT:
	Src2ObjT + (* fpt) => Src2ObjT
	Src2ObjT => Src2Obj
	Src2ObjT + (* _pt) => %10
prim2PrimF:
	prim2PrimF => Prim2PrimF
%16:
	%16 + (* _loadPrimCtxtArr) => Sink2Prim
Prim2PrimF:
	_Prim2PrimF + (Sink2Prim *) => SinkF2Prim
%11:
	%11 + (* _loadPrimCtxtArr) => Src2Prim
Ref2PrimF:
	_Ref2PrimF + (Sink2Prim *) => %2
%18:
	%18[i] + (* pt) => Sink2PrimFld[i]
%5:
	%5 + (* pt) => Src2ObjX
Ref2RefT:
	Ref2RefT + (_pt *) => Obj2RefT
%10:
	%10 + (* _loadPrimCtxt) => Src2Prim
Prim2RefF:
	_Prim2RefF + (%3 *) => SinkF2Prim
refArg2PrimRetTStub:
	refArg2PrimRetTStub => Ref2PrimT
prim2PrimT:
	prim2PrimT => Prim2PrimT
Src2RefT:
	Src2RefT + (* pt) => Src2ObjT
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
  case 63:
  case 64:
  case 65:
  case 67:
  case 71:
  case 72:
  case 74:
  case 76:
    return true;
  default:
    return false;
  }
}

public int numKinds() {
  return 87;
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
  if (symbol.equals("Obj2RefT")) return 34;
  if (symbol.equals("pt")) return 35;
  if (symbol.equals("Obj2PrimT")) return 36;
  if (symbol.equals("fptArr")) return 37;
  if (symbol.equals("Src2ObjT")) return 38;
  if (symbol.equals("fpt")) return 39;
  if (symbol.equals("Sink2ObjT")) return 40;
  if (symbol.equals("SinkF2Obj")) return 41;
  if (symbol.equals("Sink2Obj")) return 42;
  if (symbol.equals("%0")) return 43;
  if (symbol.equals("%1")) return 44;
  if (symbol.equals("Sink2Prim")) return 45;
  if (symbol.equals("%2")) return 46;
  if (symbol.equals("SinkF2Prim")) return 47;
  if (symbol.equals("%3")) return 48;
  if (symbol.equals("Src2Sink")) return 49;
  if (symbol.equals("Src2Obj")) return 50;
  if (symbol.equals("Src2Prim")) return 51;
  if (symbol.equals("Src2PrimFld")) return 52;
  if (symbol.equals("Src2ObjX")) return 53;
  if (symbol.equals("%4")) return 54;
  if (symbol.equals("%5")) return 55;
  if (symbol.equals("Src2PrimFldArr")) return 56;
  if (symbol.equals("%6")) return 57;
  if (symbol.equals("Sink2ObjX")) return 58;
  if (symbol.equals("%7")) return 59;
  if (symbol.equals("%8")) return 60;
  if (symbol.equals("Sink2PrimFldArr")) return 61;
  if (symbol.equals("%9")) return 62;
  if (symbol.equals("assignPrimCtxt")) return 63;
  if (symbol.equals("assignPrimCCtxt")) return 64;
  if (symbol.equals("loadPrimCtxt")) return 65;
  if (symbol.equals("%10")) return 66;
  if (symbol.equals("loadPrimCtxtArr")) return 67;
  if (symbol.equals("%11")) return 68;
  if (symbol.equals("%12")) return 69;
  if (symbol.equals("Src2PrimFldStat")) return 70;
  if (symbol.equals("loadStatPrimCtxt")) return 71;
  if (symbol.equals("storePrimCtxt")) return 72;
  if (symbol.equals("%13")) return 73;
  if (symbol.equals("storePrimCtxtArr")) return 74;
  if (symbol.equals("%14")) return 75;
  if (symbol.equals("storeStatPrimCtxt")) return 76;
  if (symbol.equals("%15")) return 77;
  if (symbol.equals("%16")) return 78;
  if (symbol.equals("Sink2PrimFld")) return 79;
  if (symbol.equals("%17")) return 80;
  if (symbol.equals("Sink2PrimFldStat")) return 81;
  if (symbol.equals("%18")) return 82;
  if (symbol.equals("%19")) return 83;
  if (symbol.equals("LabelRef3")) return 84;
  if (symbol.equals("LabelPrim3")) return 85;
  if (symbol.equals("Flow3")) return 86;
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
  case 34: return "Obj2RefT";
  case 35: return "pt";
  case 36: return "Obj2PrimT";
  case 37: return "fptArr";
  case 38: return "Src2ObjT";
  case 39: return "fpt";
  case 40: return "Sink2ObjT";
  case 41: return "SinkF2Obj";
  case 42: return "Sink2Obj";
  case 43: return "%0";
  case 44: return "%1";
  case 45: return "Sink2Prim";
  case 46: return "%2";
  case 47: return "SinkF2Prim";
  case 48: return "%3";
  case 49: return "Src2Sink";
  case 50: return "Src2Obj";
  case 51: return "Src2Prim";
  case 52: return "Src2PrimFld";
  case 53: return "Src2ObjX";
  case 54: return "%4";
  case 55: return "%5";
  case 56: return "Src2PrimFldArr";
  case 57: return "%6";
  case 58: return "Sink2ObjX";
  case 59: return "%7";
  case 60: return "%8";
  case 61: return "Sink2PrimFldArr";
  case 62: return "%9";
  case 63: return "assignPrimCtxt";
  case 64: return "assignPrimCCtxt";
  case 65: return "loadPrimCtxt";
  case 66: return "%10";
  case 67: return "loadPrimCtxtArr";
  case 68: return "%11";
  case 69: return "%12";
  case 70: return "Src2PrimFldStat";
  case 71: return "loadStatPrimCtxt";
  case 72: return "storePrimCtxt";
  case 73: return "%13";
  case 74: return "storePrimCtxtArr";
  case 75: return "%14";
  case 76: return "storeStatPrimCtxt";
  case 77: return "%15";
  case 78: return "%16";
  case 79: return "Sink2PrimFld";
  case 80: return "%17";
  case 81: return "Sink2PrimFldStat";
  case 82: return "%18";
  case 83: return "%19";
  case 84: return "LabelRef3";
  case 85: return "LabelPrim3";
  case 86: return "Flow3";
  default: throw new RuntimeException("Unknown kind "+kind);
  }
}

public void process(Edge base) {
  switch (base.kind) {
  case 0: /* Ref2RefT */
    /* Ref2RefT + (_pt *) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(35)){
      addEdge(other.to, base.to, 34, base, other, false);
    }
    break;
  case 1: /* ref2RefT */
    /* ref2RefT => Ref2RefT */
    addEdge(base.from, base.to, 0, base, false);
    break;
  case 2: /* Ref2PrimT */
    /* Ref2PrimT + (_pt *) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(35)){
      addEdge(other.to, base.to, 36, base, other, false);
    }
    break;
  case 3: /* ref2PrimT */
    /* ref2PrimT => Ref2PrimT */
    addEdge(base.from, base.to, 2, base, false);
    break;
  case 4: /* Prim2RefT */
    /* Prim2RefT + (Src2Prim *) => %5 */
    for(Edge other : base.from.getInEdges(51)){
      addEdge(other.from, base.to, 55, base, other, false);
    }
    /* Prim2RefT + (Sink2Prim *) => %8 */
    for(Edge other : base.from.getInEdges(45)){
      addEdge(other.from, base.to, 60, base, other, false);
    }
    break;
  case 5: /* prim2RefT */
    /* prim2RefT => Prim2RefT */
    addEdge(base.from, base.to, 4, base, false);
    break;
  case 6: /* Prim2PrimT */
    /* Prim2PrimT + (Src2Prim *) => Src2Prim */
    for(Edge other : base.from.getInEdges(51)){
      addEdge(other.from, base.to, 51, base, other, false);
    }
    /* Prim2PrimT + (Sink2Prim *) => Sink2Prim */
    for(Edge other : base.from.getInEdges(45)){
      addEdge(other.from, base.to, 45, base, other, false);
    }
    break;
  case 7: /* prim2PrimT */
    /* prim2PrimT => Prim2PrimT */
    addEdge(base.from, base.to, 6, base, false);
    break;
  case 8: /* Src2RefT */
    /* Src2RefT + (* pt) => Src2ObjT */
    for(Edge other : base.to.getOutEdges(35)){
      addEdge(base.from, other.to, 38, base, other, false);
    }
    break;
  case 9: /* src2RefT */
    /* src2RefT => Src2RefT */
    addEdge(base.from, base.to, 8, base, false);
    break;
  case 10: /* Src2PrimT */
    /* Src2PrimT => Src2Prim */
    addEdge(base.from, base.to, 51, base, false);
    break;
  case 11: /* src2PrimT */
    /* src2PrimT => Src2PrimT */
    addEdge(base.from, base.to, 10, base, false);
    break;
  case 12: /* Sink2RefT */
    /* Sink2RefT + (* pt) => Sink2ObjT */
    for(Edge other : base.to.getOutEdges(35)){
      addEdge(base.from, other.to, 40, base, other, false);
    }
    break;
  case 13: /* sink2RefT */
    /* sink2RefT => Sink2RefT */
    addEdge(base.from, base.to, 12, base, false);
    break;
  case 14: /* Sink2PrimT */
    /* Sink2PrimT => Sink2Prim */
    addEdge(base.from, base.to, 45, base, false);
    break;
  case 15: /* sink2PrimT */
    /* sink2PrimT => Sink2PrimT */
    addEdge(base.from, base.to, 14, base, false);
    break;
  case 16: /* SinkF2RefF */
    /* SinkF2RefF + (* pt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(35)){
      addEdge(base.from, other.to, 41, base, other, false);
    }
    break;
  case 17: /* sinkF2RefF */
    /* sinkF2RefF => SinkF2RefF */
    addEdge(base.from, base.to, 16, base, false);
    break;
  case 18: /* SinkF2PrimF */
    /* SinkF2PrimF => SinkF2Prim */
    addEdge(base.from, base.to, 47, base, false);
    break;
  case 19: /* sinkF2PrimF */
    /* sinkF2PrimF => SinkF2PrimF */
    addEdge(base.from, base.to, 18, base, false);
    break;
  case 20: /* Ref2RefF */
    /* _Ref2RefF + (%0 *) => %1 */
    for(Edge other : base.to.getInEdges(43)){
      addEdge(other.from, base.from, 44, base, other, false);
    }
    break;
  case 21: /* ref2RefF */
    /* ref2RefF => Ref2RefF */
    addEdge(base.from, base.to, 20, base, false);
    break;
  case 22: /* Ref2PrimF */
    /* _Ref2PrimF + (Sink2Prim *) => %2 */
    for(Edge other : base.to.getInEdges(45)){
      addEdge(other.from, base.from, 46, base, other, false);
    }
    break;
  case 23: /* ref2PrimF */
    /* ref2PrimF => Ref2PrimF */
    addEdge(base.from, base.to, 22, base, false);
    break;
  case 24: /* Prim2RefF */
    /* _Prim2RefF + (%3 *) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(48)){
      addEdge(other.from, base.from, 47, base, other, false);
    }
    break;
  case 25: /* prim2RefF */
    /* prim2RefF => Prim2RefF */
    addEdge(base.from, base.to, 24, base, false);
    break;
  case 26: /* Prim2PrimF */
    /* _Prim2PrimF + (Sink2Prim *) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(45)){
      addEdge(other.from, base.from, 47, base, other, false);
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
  case 34: /* Obj2RefT */
    /* Obj2RefT + (_fptArr *) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(37)){
      addEdge(other.to, base.to, 34, base, other, false);
    }
    /* Obj2RefT + (Src2Obj *) => %4 */
    for(Edge other : base.from.getInEdges(50)){
      addEdge(other.from, base.to, 54, base, other, false);
    }
    /* Obj2RefT + (Src2PrimFldArr *) => %6 */
    for(Edge other : base.from.getInEdges(56)){
      addEdge(other.from, base.to, 57, base, other, false);
    }
    /* Obj2RefT + (Sink2Obj *) => %7 */
    for(Edge other : base.from.getInEdges(42)){
      addEdge(other.from, base.to, 59, base, other, false);
    }
    /* Obj2RefT + (Sink2PrimFldArr *) => %9 */
    for(Edge other : base.from.getInEdges(61)){
      addEdge(other.from, base.to, 62, base, other, false);
    }
    break;
  case 35: /* pt */
    /* _pt + (* Ref2RefT) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(0)){
      addEdge(base.to, other.to, 34, base, other, false);
    }
    /* _pt + (* Ref2PrimT) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(2)){
      addEdge(base.to, other.to, 36, base, other, false);
    }
    /* pt + (Src2RefT *) => Src2ObjT */
    for(Edge other : base.from.getInEdges(8)){
      addEdge(other.from, base.to, 38, base, other, false);
    }
    /* pt + (Sink2RefT *) => Sink2ObjT */
    for(Edge other : base.from.getInEdges(12)){
      addEdge(other.from, base.to, 40, base, other, false);
    }
    /* pt + (SinkF2RefF *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(16)){
      addEdge(other.from, base.to, 41, base, other, false);
    }
    /* _pt + (Sink2Obj *) => %0 */
    for(Edge other : base.to.getInEdges(42)){
      addEdge(other.from, base.from, 43, base, other, false);
    }
    /* pt + (%1 *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(44)){
      addEdge(other.from, base.to, 41, base, other, false);
    }
    /* pt + (%2 *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(46)){
      addEdge(other.from, base.to, 41, base, other, false);
    }
    /* _pt + (Sink2Obj *) => %3 */
    for(Edge other : base.to.getInEdges(42)){
      addEdge(other.from, base.from, 48, base, other, false);
    }
    /* pt + (%4 *) => Src2ObjX */
    for(Edge other : base.from.getInEdges(54)){
      addEdge(other.from, base.to, 53, base, other, false);
    }
    /* pt + (%5 *) => Src2ObjX */
    for(Edge other : base.from.getInEdges(55)){
      addEdge(other.from, base.to, 53, base, other, false);
    }
    /* pt + (%6 *) => Src2ObjX */
    for(Edge other : base.from.getInEdges(57)){
      addEdge(other.from, base.to, 53, base, other, false);
    }
    /* pt + (%7 *) => Sink2ObjX */
    for(Edge other : base.from.getInEdges(59)){
      addEdge(other.from, base.to, 58, base, other, false);
    }
    /* pt + (%8 *) => Sink2ObjX */
    for(Edge other : base.from.getInEdges(60)){
      addEdge(other.from, base.to, 58, base, other, false);
    }
    /* pt + (%9 *) => Sink2ObjX */
    for(Edge other : base.from.getInEdges(62)){
      addEdge(other.from, base.to, 58, base, other, false);
    }
    /* _pt + (Src2ObjT *) => %10 */
    for(Edge other : base.to.getInEdges(38)){
      addEdge(other.from, base.from, 66, base, other, false);
    }
    /* _pt + (Src2ObjX *) => %11 */
    for(Edge other : base.to.getInEdges(53)){
      addEdge(other.from, base.from, 68, base, other, false);
    }
    /* _pt + (Src2PrimFld[i] *) => %12[i] */
    for(Edge other : base.to.getInEdges(52)){
      addEdge(other.from, base.from, 69, base, other, true);
    }
    /* pt + (%13[i] *) => Src2PrimFld[i] */
    for(Edge other : base.from.getInEdges(73)){
      addEdge(other.from, base.to, 52, base, other, true);
    }
    /* pt + (%14 *) => Src2PrimFldArr */
    for(Edge other : base.from.getInEdges(75)){
      addEdge(other.from, base.to, 56, base, other, false);
    }
    /* _pt + (Sink2ObjT *) => %15 */
    for(Edge other : base.to.getInEdges(40)){
      addEdge(other.from, base.from, 77, base, other, false);
    }
    /* _pt + (Sink2ObjX *) => %16 */
    for(Edge other : base.to.getInEdges(58)){
      addEdge(other.from, base.from, 78, base, other, false);
    }
    /* _pt + (Sink2PrimFld[i] *) => %17[i] */
    for(Edge other : base.to.getInEdges(79)){
      addEdge(other.from, base.from, 80, base, other, true);
    }
    /* pt + (%18[i] *) => Sink2PrimFld[i] */
    for(Edge other : base.from.getInEdges(82)){
      addEdge(other.from, base.to, 79, base, other, true);
    }
    /* pt + (%19 *) => Sink2PrimFldArr */
    for(Edge other : base.from.getInEdges(83)){
      addEdge(other.from, base.to, 61, base, other, false);
    }
    /* _pt + (Src2Obj *) => LabelRef3 */
    for(Edge other : base.to.getInEdges(50)){
      addEdge(other.from, base.from, 84, base, other, false);
    }
    /* _pt + (Sink2Obj *) => LabelRef3 */
    for(Edge other : base.to.getInEdges(42)){
      addEdge(other.from, base.from, 84, base, other, false);
    }
    break;
  case 36: /* Obj2PrimT */
    /* Obj2PrimT + (_fptArr *) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(37)){
      addEdge(other.to, base.to, 36, base, other, false);
    }
    /* Obj2PrimT + (Src2Obj *) => Src2Prim */
    for(Edge other : base.from.getInEdges(50)){
      addEdge(other.from, base.to, 51, base, other, false);
    }
    /* Obj2PrimT + (Src2PrimFldArr *) => Src2Prim */
    for(Edge other : base.from.getInEdges(56)){
      addEdge(other.from, base.to, 51, base, other, false);
    }
    /* Obj2PrimT + (Sink2Obj *) => Sink2Prim */
    for(Edge other : base.from.getInEdges(42)){
      addEdge(other.from, base.to, 45, base, other, false);
    }
    /* Obj2PrimT + (Sink2PrimFldArr *) => Sink2Prim */
    for(Edge other : base.from.getInEdges(61)){
      addEdge(other.from, base.to, 45, base, other, false);
    }
    break;
  case 37: /* fptArr */
    /* _fptArr + (* Obj2RefT) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(34)){
      addEdge(base.to, other.to, 34, base, other, false);
    }
    /* _fptArr + (* Obj2PrimT) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(36)){
      addEdge(base.to, other.to, 36, base, other, false);
    }
    /* fptArr + (Src2ObjX *) => Src2ObjX */
    for(Edge other : base.from.getInEdges(53)){
      addEdge(other.from, base.to, 53, base, other, false);
    }
    /* fptArr + (Sink2ObjX *) => Sink2ObjX */
    for(Edge other : base.from.getInEdges(58)){
      addEdge(other.from, base.to, 58, base, other, false);
    }
    break;
  case 38: /* Src2ObjT */
    /* Src2ObjT + (* fpt) => Src2ObjT */
    for(Edge other : base.to.getOutEdges(39)){
      addEdge(base.from, other.to, 38, base, other, false);
    }
    /* Src2ObjT => Src2Obj */
    addEdge(base.from, base.to, 50, base, false);
    /* Src2ObjT + (* _pt) => %10 */
    for(Edge other : base.to.getInEdges(35)){
      addEdge(base.from, other.from, 66, base, other, false);
    }
    break;
  case 39: /* fpt */
    /* fpt + (Src2ObjT *) => Src2ObjT */
    for(Edge other : base.from.getInEdges(38)){
      addEdge(other.from, base.to, 38, base, other, false);
    }
    /* fpt + (Sink2ObjT *) => Sink2ObjT */
    for(Edge other : base.from.getInEdges(40)){
      addEdge(other.from, base.to, 40, base, other, false);
    }
    /* fpt + (SinkF2Obj *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(41)){
      addEdge(other.from, base.to, 41, base, other, false);
    }
    break;
  case 40: /* Sink2ObjT */
    /* Sink2ObjT + (* fpt) => Sink2ObjT */
    for(Edge other : base.to.getOutEdges(39)){
      addEdge(base.from, other.to, 40, base, other, false);
    }
    /* Sink2ObjT => Sink2Obj */
    addEdge(base.from, base.to, 42, base, false);
    /* Sink2ObjT + (* _pt) => %15 */
    for(Edge other : base.to.getInEdges(35)){
      addEdge(base.from, other.from, 77, base, other, false);
    }
    break;
  case 41: /* SinkF2Obj */
    /* SinkF2Obj + (* fpt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(39)){
      addEdge(base.from, other.to, 41, base, other, false);
    }
    /* _SinkF2Obj + (Src2Obj *) => Src2Sink */
    for(Edge other : base.to.getInEdges(50)){
      addEdge(other.from, base.from, 49, base, other, false);
    }
    /* _SinkF2Obj + (Src2PrimFld *) => Src2Sink */
    for(Edge other : base.to.getInEdges(52)){
      addEdge(other.from, base.from, 49, base, other, false);
    }
    break;
  case 42: /* Sink2Obj */
    /* Sink2Obj + (* _pt) => %0 */
    for(Edge other : base.to.getInEdges(35)){
      addEdge(base.from, other.from, 43, base, other, false);
    }
    /* Sink2Obj + (* _pt) => %3 */
    for(Edge other : base.to.getInEdges(35)){
      addEdge(base.from, other.from, 48, base, other, false);
    }
    /* Sink2Obj + (* Obj2RefT) => %7 */
    for(Edge other : base.to.getOutEdges(34)){
      addEdge(base.from, other.to, 59, base, other, false);
    }
    /* Sink2Obj + (* Obj2PrimT) => Sink2Prim */
    for(Edge other : base.to.getOutEdges(36)){
      addEdge(base.from, other.to, 45, base, other, false);
    }
    /* Sink2Obj + (* _pt) => LabelRef3 */
    for(Edge other : base.to.getInEdges(35)){
      addEdge(base.from, other.from, 84, base, other, false);
    }
    break;
  case 43: /* %0 */
    /* %0 + (* _Ref2RefF) => %1 */
    for(Edge other : base.to.getInEdges(20)){
      addEdge(base.from, other.from, 44, base, other, false);
    }
    break;
  case 44: /* %1 */
    /* %1 + (* pt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(35)){
      addEdge(base.from, other.to, 41, base, other, false);
    }
    break;
  case 45: /* Sink2Prim */
    /* Sink2Prim + (* _Ref2PrimF) => %2 */
    for(Edge other : base.to.getInEdges(22)){
      addEdge(base.from, other.from, 46, base, other, false);
    }
    /* Sink2Prim + (* _Prim2PrimF) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(26)){
      addEdge(base.from, other.from, 47, base, other, false);
    }
    /* Sink2Prim + (* Prim2RefT) => %8 */
    for(Edge other : base.to.getOutEdges(4)){
      addEdge(base.from, other.to, 60, base, other, false);
    }
    /* Sink2Prim + (* _assignPrimCtxt) => Sink2Prim */
    for(Edge other : base.to.getInEdges(63)){
      addEdge(base.from, other.from, 45, base, other, false);
    }
    /* Sink2Prim + (* _assignPrimCCtxt) => Sink2Prim */
    for(Edge other : base.to.getInEdges(64)){
      addEdge(base.from, other.from, 45, base, other, false);
    }
    /* Sink2Prim + (* Prim2PrimT) => Sink2Prim */
    for(Edge other : base.to.getOutEdges(6)){
      addEdge(base.from, other.to, 45, base, other, false);
    }
    /* Sink2Prim + (* _storePrimCtxt[i]) => %18[i] */
    for(Edge other : base.to.getInEdges(72)){
      addEdge(base.from, other.from, 82, base, other, true);
    }
    /* Sink2Prim + (* _storePrimCtxtArr) => %19 */
    for(Edge other : base.to.getInEdges(74)){
      addEdge(base.from, other.from, 83, base, other, false);
    }
    /* Sink2Prim + (* _storeStatPrimCtxt[i]) => Sink2PrimFldStat[i] */
    for(Edge other : base.to.getInEdges(76)){
      addEdge(base.from, other.from, 81, base, other, true);
    }
    /* Sink2Prim => LabelPrim3 */
    addEdge(base.from, base.to, 85, base, false);
    break;
  case 46: /* %2 */
    /* %2 + (* pt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(35)){
      addEdge(base.from, other.to, 41, base, other, false);
    }
    break;
  case 47: /* SinkF2Prim */
    /* _SinkF2Prim + (Src2Prim *) => Src2Sink */
    for(Edge other : base.to.getInEdges(51)){
      addEdge(other.from, base.from, 49, base, other, false);
    }
    break;
  case 48: /* %3 */
    /* %3 + (* _Prim2RefF) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(24)){
      addEdge(base.from, other.from, 47, base, other, false);
    }
    break;
  case 49: /* Src2Sink */
    /* Src2Sink => Flow3 */
    addEdge(base.from, base.to, 86, base, false);
    break;
  case 50: /* Src2Obj */
    /* Src2Obj + (* _SinkF2Obj) => Src2Sink */
    for(Edge other : base.to.getInEdges(41)){
      addEdge(base.from, other.from, 49, base, other, false);
    }
    /* Src2Obj + (* Obj2RefT) => %4 */
    for(Edge other : base.to.getOutEdges(34)){
      addEdge(base.from, other.to, 54, base, other, false);
    }
    /* Src2Obj + (* Obj2PrimT) => Src2Prim */
    for(Edge other : base.to.getOutEdges(36)){
      addEdge(base.from, other.to, 51, base, other, false);
    }
    /* Src2Obj + (* _pt) => LabelRef3 */
    for(Edge other : base.to.getInEdges(35)){
      addEdge(base.from, other.from, 84, base, other, false);
    }
    break;
  case 51: /* Src2Prim */
    /* Src2Prim + (* _SinkF2Prim) => Src2Sink */
    for(Edge other : base.to.getInEdges(47)){
      addEdge(base.from, other.from, 49, base, other, false);
    }
    /* Src2Prim + (* Prim2RefT) => %5 */
    for(Edge other : base.to.getOutEdges(4)){
      addEdge(base.from, other.to, 55, base, other, false);
    }
    /* Src2Prim + (* _assignPrimCtxt) => Src2Prim */
    for(Edge other : base.to.getInEdges(63)){
      addEdge(base.from, other.from, 51, base, other, false);
    }
    /* Src2Prim + (* _assignPrimCCtxt) => Src2Prim */
    for(Edge other : base.to.getInEdges(64)){
      addEdge(base.from, other.from, 51, base, other, false);
    }
    /* Src2Prim + (* Prim2PrimT) => Src2Prim */
    for(Edge other : base.to.getOutEdges(6)){
      addEdge(base.from, other.to, 51, base, other, false);
    }
    /* Src2Prim + (* _storePrimCtxt[i]) => %13[i] */
    for(Edge other : base.to.getInEdges(72)){
      addEdge(base.from, other.from, 73, base, other, true);
    }
    /* Src2Prim + (* _storePrimCtxtArr) => %14 */
    for(Edge other : base.to.getInEdges(74)){
      addEdge(base.from, other.from, 75, base, other, false);
    }
    /* Src2Prim + (* _storeStatPrimCtxt[i]) => Src2PrimFldStat[i] */
    for(Edge other : base.to.getInEdges(76)){
      addEdge(base.from, other.from, 70, base, other, true);
    }
    /* Src2Prim => LabelPrim3 */
    addEdge(base.from, base.to, 85, base, false);
    break;
  case 52: /* Src2PrimFld */
    /* Src2PrimFld + (* _SinkF2Obj) => Src2Sink */
    for(Edge other : base.to.getInEdges(41)){
      addEdge(base.from, other.from, 49, base, other, false);
    }
    /* Src2PrimFld[i] + (* _pt) => %12[i] */
    for(Edge other : base.to.getInEdges(35)){
      addEdge(base.from, other.from, 69, base, other, true);
    }
    break;
  case 53: /* Src2ObjX */
    /* Src2ObjX => Src2Obj */
    addEdge(base.from, base.to, 50, base, false);
    /* Src2ObjX + (* fptArr) => Src2ObjX */
    for(Edge other : base.to.getOutEdges(37)){
      addEdge(base.from, other.to, 53, base, other, false);
    }
    /* Src2ObjX + (* _pt) => %11 */
    for(Edge other : base.to.getInEdges(35)){
      addEdge(base.from, other.from, 68, base, other, false);
    }
    break;
  case 54: /* %4 */
    /* %4 + (* pt) => Src2ObjX */
    for(Edge other : base.to.getOutEdges(35)){
      addEdge(base.from, other.to, 53, base, other, false);
    }
    break;
  case 55: /* %5 */
    /* %5 + (* pt) => Src2ObjX */
    for(Edge other : base.to.getOutEdges(35)){
      addEdge(base.from, other.to, 53, base, other, false);
    }
    break;
  case 56: /* Src2PrimFldArr */
    /* Src2PrimFldArr + (* Obj2RefT) => %6 */
    for(Edge other : base.to.getOutEdges(34)){
      addEdge(base.from, other.to, 57, base, other, false);
    }
    /* Src2PrimFldArr + (* Obj2PrimT) => Src2Prim */
    for(Edge other : base.to.getOutEdges(36)){
      addEdge(base.from, other.to, 51, base, other, false);
    }
    break;
  case 57: /* %6 */
    /* %6 + (* pt) => Src2ObjX */
    for(Edge other : base.to.getOutEdges(35)){
      addEdge(base.from, other.to, 53, base, other, false);
    }
    break;
  case 58: /* Sink2ObjX */
    /* Sink2ObjX => Sink2Obj */
    addEdge(base.from, base.to, 42, base, false);
    /* Sink2ObjX + (* fptArr) => Sink2ObjX */
    for(Edge other : base.to.getOutEdges(37)){
      addEdge(base.from, other.to, 58, base, other, false);
    }
    /* Sink2ObjX + (* _pt) => %16 */
    for(Edge other : base.to.getInEdges(35)){
      addEdge(base.from, other.from, 78, base, other, false);
    }
    break;
  case 59: /* %7 */
    /* %7 + (* pt) => Sink2ObjX */
    for(Edge other : base.to.getOutEdges(35)){
      addEdge(base.from, other.to, 58, base, other, false);
    }
    break;
  case 60: /* %8 */
    /* %8 + (* pt) => Sink2ObjX */
    for(Edge other : base.to.getOutEdges(35)){
      addEdge(base.from, other.to, 58, base, other, false);
    }
    break;
  case 61: /* Sink2PrimFldArr */
    /* Sink2PrimFldArr + (* Obj2RefT) => %9 */
    for(Edge other : base.to.getOutEdges(34)){
      addEdge(base.from, other.to, 62, base, other, false);
    }
    /* Sink2PrimFldArr + (* Obj2PrimT) => Sink2Prim */
    for(Edge other : base.to.getOutEdges(36)){
      addEdge(base.from, other.to, 45, base, other, false);
    }
    break;
  case 62: /* %9 */
    /* %9 + (* pt) => Sink2ObjX */
    for(Edge other : base.to.getOutEdges(35)){
      addEdge(base.from, other.to, 58, base, other, false);
    }
    break;
  case 63: /* assignPrimCtxt */
    /* _assignPrimCtxt + (Src2Prim *) => Src2Prim */
    for(Edge other : base.to.getInEdges(51)){
      addEdge(other.from, base.from, 51, base, other, false);
    }
    /* _assignPrimCtxt + (Sink2Prim *) => Sink2Prim */
    for(Edge other : base.to.getInEdges(45)){
      addEdge(other.from, base.from, 45, base, other, false);
    }
    break;
  case 64: /* assignPrimCCtxt */
    /* _assignPrimCCtxt + (Src2Prim *) => Src2Prim */
    for(Edge other : base.to.getInEdges(51)){
      addEdge(other.from, base.from, 51, base, other, false);
    }
    /* _assignPrimCCtxt + (Sink2Prim *) => Sink2Prim */
    for(Edge other : base.to.getInEdges(45)){
      addEdge(other.from, base.from, 45, base, other, false);
    }
    break;
  case 65: /* loadPrimCtxt */
    /* _loadPrimCtxt + (%10 *) => Src2Prim */
    for(Edge other : base.to.getInEdges(66)){
      addEdge(other.from, base.from, 51, base, other, false);
    }
    /* _loadPrimCtxt[i] + (%12[i] *) => Src2Prim */
    for(Edge other : base.to.getInEdges(69)){
      addEdge(other.from, base.from, 51, base, other, false);
    }
    /* _loadPrimCtxt + (%15 *) => Sink2Prim */
    for(Edge other : base.to.getInEdges(77)){
      addEdge(other.from, base.from, 45, base, other, false);
    }
    /* _loadPrimCtxt[i] + (%17[i] *) => Sink2Prim */
    for(Edge other : base.to.getInEdges(80)){
      addEdge(other.from, base.from, 45, base, other, false);
    }
    break;
  case 66: /* %10 */
    /* %10 + (* _loadPrimCtxt) => Src2Prim */
    for(Edge other : base.to.getInEdges(65)){
      addEdge(base.from, other.from, 51, base, other, false);
    }
    break;
  case 67: /* loadPrimCtxtArr */
    /* _loadPrimCtxtArr + (%11 *) => Src2Prim */
    for(Edge other : base.to.getInEdges(68)){
      addEdge(other.from, base.from, 51, base, other, false);
    }
    /* _loadPrimCtxtArr + (%16 *) => Sink2Prim */
    for(Edge other : base.to.getInEdges(78)){
      addEdge(other.from, base.from, 45, base, other, false);
    }
    break;
  case 68: /* %11 */
    /* %11 + (* _loadPrimCtxtArr) => Src2Prim */
    for(Edge other : base.to.getInEdges(67)){
      addEdge(base.from, other.from, 51, base, other, false);
    }
    break;
  case 69: /* %12 */
    /* %12[i] + (* _loadPrimCtxt[i]) => Src2Prim */
    for(Edge other : base.to.getInEdges(65)){
      addEdge(base.from, other.from, 51, base, other, false);
    }
    break;
  case 70: /* Src2PrimFldStat */
    /* Src2PrimFldStat[i] + (* _loadStatPrimCtxt[i]) => Src2Prim */
    for(Edge other : base.to.getInEdges(71)){
      addEdge(base.from, other.from, 51, base, other, false);
    }
    break;
  case 71: /* loadStatPrimCtxt */
    /* _loadStatPrimCtxt[i] + (Src2PrimFldStat[i] *) => Src2Prim */
    for(Edge other : base.to.getInEdges(70)){
      addEdge(other.from, base.from, 51, base, other, false);
    }
    /* _loadStatPrimCtxt[i] + (Sink2PrimFldStat[i] *) => Sink2Prim */
    for(Edge other : base.to.getInEdges(81)){
      addEdge(other.from, base.from, 45, base, other, false);
    }
    break;
  case 72: /* storePrimCtxt */
    /* _storePrimCtxt[i] + (Src2Prim *) => %13[i] */
    for(Edge other : base.to.getInEdges(51)){
      addEdge(other.from, base.from, 73, base, other, true);
    }
    /* _storePrimCtxt[i] + (Sink2Prim *) => %18[i] */
    for(Edge other : base.to.getInEdges(45)){
      addEdge(other.from, base.from, 82, base, other, true);
    }
    break;
  case 73: /* %13 */
    /* %13[i] + (* pt) => Src2PrimFld[i] */
    for(Edge other : base.to.getOutEdges(35)){
      addEdge(base.from, other.to, 52, base, other, true);
    }
    break;
  case 74: /* storePrimCtxtArr */
    /* _storePrimCtxtArr + (Src2Prim *) => %14 */
    for(Edge other : base.to.getInEdges(51)){
      addEdge(other.from, base.from, 75, base, other, false);
    }
    /* _storePrimCtxtArr + (Sink2Prim *) => %19 */
    for(Edge other : base.to.getInEdges(45)){
      addEdge(other.from, base.from, 83, base, other, false);
    }
    break;
  case 75: /* %14 */
    /* %14 + (* pt) => Src2PrimFldArr */
    for(Edge other : base.to.getOutEdges(35)){
      addEdge(base.from, other.to, 56, base, other, false);
    }
    break;
  case 76: /* storeStatPrimCtxt */
    /* _storeStatPrimCtxt[i] + (Src2Prim *) => Src2PrimFldStat[i] */
    for(Edge other : base.to.getInEdges(51)){
      addEdge(other.from, base.from, 70, base, other, true);
    }
    /* _storeStatPrimCtxt[i] + (Sink2Prim *) => Sink2PrimFldStat[i] */
    for(Edge other : base.to.getInEdges(45)){
      addEdge(other.from, base.from, 81, base, other, true);
    }
    break;
  case 77: /* %15 */
    /* %15 + (* _loadPrimCtxt) => Sink2Prim */
    for(Edge other : base.to.getInEdges(65)){
      addEdge(base.from, other.from, 45, base, other, false);
    }
    break;
  case 78: /* %16 */
    /* %16 + (* _loadPrimCtxtArr) => Sink2Prim */
    for(Edge other : base.to.getInEdges(67)){
      addEdge(base.from, other.from, 45, base, other, false);
    }
    break;
  case 79: /* Sink2PrimFld */
    /* Sink2PrimFld[i] + (* _pt) => %17[i] */
    for(Edge other : base.to.getInEdges(35)){
      addEdge(base.from, other.from, 80, base, other, true);
    }
    break;
  case 80: /* %17 */
    /* %17[i] + (* _loadPrimCtxt[i]) => Sink2Prim */
    for(Edge other : base.to.getInEdges(65)){
      addEdge(base.from, other.from, 45, base, other, false);
    }
    break;
  case 81: /* Sink2PrimFldStat */
    /* Sink2PrimFldStat[i] + (* _loadStatPrimCtxt[i]) => Sink2Prim */
    for(Edge other : base.to.getInEdges(71)){
      addEdge(base.from, other.from, 45, base, other, false);
    }
    break;
  case 82: /* %18 */
    /* %18[i] + (* pt) => Sink2PrimFld[i] */
    for(Edge other : base.to.getOutEdges(35)){
      addEdge(base.from, other.to, 79, base, other, true);
    }
    break;
  case 83: /* %19 */
    /* %19 + (* pt) => Sink2PrimFldArr */
    for(Edge other : base.to.getOutEdges(35)){
      addEdge(base.from, other.to, 61, base, other, false);
    }
    break;
  }
}

public String[] outputRels() {
    String[] rels = {};
    return rels;
}

public short kindToWeight(int kind) {
  switch (kind) {
  default:
    return (short)0;
  }
}

public boolean useReps() { return true; }

}