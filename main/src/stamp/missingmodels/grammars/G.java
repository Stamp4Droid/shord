package stamp.missingmodels.grammars;
import stamp.missingmodels.util.jcflsolver.*;

/* Original Grammar:
###################
# CONFIGURATION
###################

.output LabelRef3
.output LabelPrim3
.output Flow3

.weights ref2RefArgTStub 1
.weights ref2RefRetTStub 1

.weights prim2RefArgTStub 1
.weights prim2RefRetTStub 1

.weights ref2PrimTStub 1

.weights prim2PrimT 1

.weights transfer 1

###################
# INPUTS
###################

# src and sink annotations: src2Label, sink2Label
# label annotations: label2RefT, label2PrimT
# sinkf annotations: sink2RefT, sink2PrimT, sinkF2RefF, sinkF2PrimF
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

#Ref2RefT :: ref2RefT
#Ref2PrimT :: ref2PrimT
#Prim2RefT :: prim2RefT
#Prim2PrimT :: prim2PrimT

Label2RefT :: label2RefT
Label2PrimT :: label2PrimT

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

Ref2RefT :: ref2RefArgTStub
Ref2RefT :: ref2RefRetTStub

Prim2RefT :: prim2RefArgTStub
Prim2RefT :: prim2RefRetTStub

Ref2PrimT :: ref2PrimTStub
Prim2PrimT :: prim2PrimTStub

###################
# RULES: PARTIAL FLOW PROPAGATION
###################

PreFlowsTo :: preFlowsTo
PostFlowsTo :: postFlowsTo
MidFlowsTo :: midFlowsTo

Transfer :: transfer
Transfer :: transferSelf

PreFlowsTo :: PreFlowsTo Transfer MidFlowsTo

Pt :: _PostFlowsTo _Transfer _PreFlowsTo

Fpt[f] :: _Pt Store[f] pt
Fpt[f] :: _pt Store[f] Pt
Fpt[f] :: _Pt Store[f] Pt
FptArr :: _Pt StoreArr pt
FptArr :: _pt StoreArr Pt
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

Label2ObjT :: Label2RefT Pt
Label2ObjT :: Label2ObjT Fpt[*]

###################
# RULES: SINKF
###################

# Sink_full-obj flow

SinkF2Obj :: SinkF2RefF Pt
SinkF2Obj :: sink2Label Label2Obj _Pt _Ref2RefF Pt
SinkF2Obj :: sink2Label Label2Prim _Ref2PrimF Pt
SinkF2Obj :: SinkF2Obj Fpt[*]

# Sink_full-prim flow

SinkF2Prim :: SinkF2PrimF
SinkF2Prim :: sink2Label Label2Obj _Pt _Prim2RefF
SinkF2Prim :: sink2Label Label2Prim _Prim2PrimF

###################
# RULES: SRC-SINK FLOW
###################

Src2Sink :: src2Label Label2Obj _SinkF2Obj
Src2Sink :: src2Label Label2Prim _SinkF2Prim
Src2Sink :: src2Label Label2PrimFld[*] _SinkF2Obj

###################
# RULES: LABEL FLOW
###################

# Label-obj flow

Label2Obj :: Label2ObjT
Label2Obj :: Label2ObjX

Label2ObjX :: Label2Obj Obj2RefT Pt
Label2ObjX :: Label2Prim Prim2RefT Pt
Label2ObjX :: Label2PrimFldArr Obj2RefT Pt
Label2ObjX :: Label2ObjX FptArr

# Label-prim flow

Label2Prim :: Label2PrimT
Label2Prim :: Label2Prim _assignPrimCtxt
Label2Prim :: Label2Prim _assignPrimCCtxt

Label2Prim :: Label2Obj Obj2PrimT
Label2Prim :: Label2Prim Prim2PrimT

Label2Prim :: Label2ObjT _Pt _loadPrimCtxt[*]
Label2Prim :: Label2ObjX _Pt _loadPrimCtxtArr
Label2Prim :: Label2PrimFldArr Obj2PrimT

# cl Label2PrimFld[f] o _Pt v_c _loadPrimCtxt[f] u_c
Label2Prim :: Label2PrimFld[f] _Pt _loadPrimCtxt[f]
Label2Prim :: Label2PrimFldStat[f] _loadStatPrimCtxt[f]

# Label-prim_fld flow

Label2PrimFld[f] :: Label2Prim _storePrimCtxt[f] Pt
Label2PrimFldArr :: Label2Prim _storePrimCtxtArr Pt
Label2PrimFldStat[f] :: Label2Prim _storeStatPrimCtxt[f]

###################
# RULES: OUTPUT
###################

LabelRef3 :: Label2Obj _Pt
LabelPrim3 :: Label2Prim
Flow3 :: Src2Sink
*/

/* Normalized Grammar:
Label2Obj:
	Label2Obj :: Label2ObjT
	Label2Obj :: Label2ObjX
PostFlowsTo:
	PostFlowsTo :: postFlowsTo
Pt:
	Pt :: %1 _PreFlowsTo
	Pt :: pt
Transfer:
	Transfer :: transfer
	Transfer :: transferSelf
Flow3:
	Flow3 :: Src2Sink
%14:
	%14 :: %13 _Pt
Fpt:
	Fpt[i] :: %2[i] pt
	Fpt[i] :: %3[i] Pt
	Fpt[i] :: %4[i] Pt
	Fpt[i] :: fpt[i]
%9:
	%9 :: %8 _Pt
%8:
	%8 :: sink2Label Label2Obj
%5:
	%5 :: _Pt StoreArr
%4:
	%4[i] :: _Pt Store[i]
%7:
	%7 :: _Pt StoreArr
%6:
	%6 :: _pt StoreArr
%1:
	%1 :: _PostFlowsTo _Transfer
%0:
	%0 :: PreFlowsTo Transfer
%3:
	%3[i] :: _pt Store[i]
%2:
	%2[i] :: _Pt Store[i]
%24:
	%24[i] :: Label2PrimFld[i] _Pt
%25:
	%25[i] :: Label2Prim _storePrimCtxt[i]
%26:
	%26 :: Label2Prim _storePrimCtxtArr
%20:
	%20 :: Label2Prim Prim2RefT
%21:
	%21 :: Label2PrimFldArr Obj2RefT
%22:
	%22 :: Label2ObjT _Pt
Label2RefT:
	Label2RefT :: label2RefT
FptArr:
	FptArr :: %5 pt
	FptArr :: %6 Pt
	FptArr :: %7 Pt
	FptArr :: fptArr
Label2PrimFldStat:
	Label2PrimFldStat[i] :: Label2Prim _storeStatPrimCtxt[i]
SinkF2PrimF:
	SinkF2PrimF :: sinkF2PrimF
Label2ObjT:
	Label2ObjT :: Label2RefT Pt
	Label2ObjT :: Label2ObjT Fpt
PreFlowsTo:
	PreFlowsTo :: preFlowsTo
	PreFlowsTo :: %0 MidFlowsTo
Label2ObjX:
	Label2ObjX :: %19 Pt
	Label2ObjX :: %20 Pt
	Label2ObjX :: %21 Pt
	Label2ObjX :: Label2ObjX FptArr
%19:
	%19 :: Label2Obj Obj2RefT
Label2PrimFld:
	Label2PrimFld[i] :: %25[i] Pt
Src2Sink:
	Src2Sink :: %16 _SinkF2Obj
	Src2Sink :: %17 _SinkF2Prim
	Src2Sink :: %18 _SinkF2Obj
Label2PrimFldArr:
	Label2PrimFldArr :: %26 Pt
Prim2PrimF:
	Prim2PrimF :: prim2PrimF
LabelPrim3:
	LabelPrim3 :: Label2Prim
SinkF2RefF:
	SinkF2RefF :: sinkF2RefF
Obj2PrimT:
	Obj2PrimT :: _Pt Ref2PrimT
	Obj2PrimT :: _FptArr Obj2PrimT
Prim2PrimT:
	Prim2PrimT :: prim2PrimTStub
Label2Prim:
	Label2Prim :: Label2PrimT
	Label2Prim :: Label2Prim _assignPrimCtxt
	Label2Prim :: Label2Prim _assignPrimCCtxt
	Label2Prim :: Label2Obj Obj2PrimT
	Label2Prim :: Label2Prim Prim2PrimT
	Label2Prim :: %22 _loadPrimCtxt
	Label2Prim :: %23 _loadPrimCtxtArr
	Label2Prim :: Label2PrimFldArr Obj2PrimT
	Label2Prim :: %24[i] _loadPrimCtxt[i]
	Label2Prim :: Label2PrimFldStat[i] _loadStatPrimCtxt[i]
Obj2RefT:
	Obj2RefT :: _Pt Ref2RefT
	Obj2RefT :: _FptArr Obj2RefT
Ref2PrimT:
	Ref2PrimT :: ref2PrimTStub
%18:
	%18 :: src2Label Label2PrimFld
SinkF2Prim:
	SinkF2Prim :: SinkF2PrimF
	SinkF2Prim :: %14 _Prim2RefF
	SinkF2Prim :: %15 _Prim2PrimF
Ref2RefF:
	Ref2RefF :: ref2RefF
%23:
	%23 :: Label2ObjX _Pt
%11:
	%11 :: sink2Label Label2Prim
%10:
	%10 :: %9 _Ref2RefF
Prim2RefT:
	Prim2RefT :: prim2RefArgTStub
	Prim2RefT :: prim2RefRetTStub
%12:
	%12 :: %11 _Ref2PrimF
%15:
	%15 :: sink2Label Label2Prim
SinkF2Obj:
	SinkF2Obj :: SinkF2RefF Pt
	SinkF2Obj :: %10 Pt
	SinkF2Obj :: %12 Pt
	SinkF2Obj :: SinkF2Obj Fpt
%17:
	%17 :: src2Label Label2Prim
%16:
	%16 :: src2Label Label2Obj
Ref2PrimF:
	Ref2PrimF :: ref2PrimF
Label2PrimT:
	Label2PrimT :: label2PrimT
Ref2RefT:
	Ref2RefT :: ref2RefArgTStub
	Ref2RefT :: ref2RefRetTStub
Prim2RefF:
	Prim2RefF :: prim2RefF
MidFlowsTo:
	MidFlowsTo :: midFlowsTo
%13:
	%13 :: sink2Label Label2Obj
LabelRef3:
	LabelRef3 :: Label2Obj _Pt
*/

/* Reverse Productions:
storePrimCtxtArr:
	_storePrimCtxtArr + (Label2Prim *) => %26
%9:
	%9 + (* _Ref2RefF) => %10
%8:
	%8 + (* _Pt) => %9
%5:
	%5 + (* pt) => FptArr
%4:
	%4[i] + (* Pt) => Fpt[i]
%7:
	%7 + (* Pt) => FptArr
%6:
	%6 + (* Pt) => FptArr
%1:
	%1 + (* _PreFlowsTo) => Pt
%0:
	%0 + (* MidFlowsTo) => PreFlowsTo
%3:
	%3[i] + (* Pt) => Fpt[i]
%2:
	%2[i] + (* pt) => Fpt[i]
postFlowsTo:
	postFlowsTo => PostFlowsTo
Label2ObjT:
	Label2ObjT + (* Fpt) => Label2ObjT
	Label2ObjT => Label2Obj
	Label2ObjT + (* _Pt) => %22
Label2ObjX:
	Label2ObjX => Label2Obj
	Label2ObjX + (* FptArr) => Label2ObjX
	Label2ObjX + (* _Pt) => %23
label2RefT:
	label2RefT => Label2RefT
storePrimCtxt:
	_storePrimCtxt[i] + (Label2Prim *) => %25[i]
MidFlowsTo:
	MidFlowsTo + (%0 *) => PreFlowsTo
preFlowsTo:
	preFlowsTo => PreFlowsTo
Ref2RefF:
	_Ref2RefF + (%9 *) => %10
assignPrimCCtxt:
	_assignPrimCCtxt + (Label2Prim *) => Label2Prim
SinkF2Obj:
	SinkF2Obj + (* Fpt) => SinkF2Obj
	_SinkF2Obj + (%16 *) => Src2Sink
	_SinkF2Obj + (%18 *) => Src2Sink
Label2Prim:
	Label2Prim + (sink2Label *) => %11
	Label2Prim + (sink2Label *) => %15
	Label2Prim + (src2Label *) => %17
	Label2Prim + (* Prim2RefT) => %20
	Label2Prim + (* _assignPrimCtxt) => Label2Prim
	Label2Prim + (* _assignPrimCCtxt) => Label2Prim
	Label2Prim + (* Prim2PrimT) => Label2Prim
	Label2Prim + (* _storePrimCtxt[i]) => %25[i]
	Label2Prim + (* _storePrimCtxtArr) => %26
	Label2Prim + (* _storeStatPrimCtxt[i]) => Label2PrimFldStat[i]
	Label2Prim => LabelPrim3
Label2PrimT:
	Label2PrimT => Label2Prim
Ref2RefT:
	Ref2RefT + (_Pt *) => Obj2RefT
StoreArr:
	StoreArr + (_Pt *) => %5
	StoreArr + (_pt *) => %6
	StoreArr + (_Pt *) => %7
Pt:
	_Pt + (* Store[i]) => %2[i]
	Pt + (%3[i] *) => Fpt[i]
	_Pt + (* Store[i]) => %4[i]
	Pt + (%4[i] *) => Fpt[i]
	_Pt + (* StoreArr) => %5
	Pt + (%6 *) => FptArr
	_Pt + (* StoreArr) => %7
	Pt + (%7 *) => FptArr
	_Pt + (* Ref2RefT) => Obj2RefT
	_Pt + (* Ref2PrimT) => Obj2PrimT
	Pt + (Label2RefT *) => Label2ObjT
	Pt + (SinkF2RefF *) => SinkF2Obj
	_Pt + (%8 *) => %9
	Pt + (%10 *) => SinkF2Obj
	Pt + (%12 *) => SinkF2Obj
	_Pt + (%13 *) => %14
	Pt + (%19 *) => Label2ObjX
	Pt + (%20 *) => Label2ObjX
	Pt + (%21 *) => Label2ObjX
	_Pt + (Label2ObjT *) => %22
	_Pt + (Label2ObjX *) => %23
	_Pt + (Label2PrimFld[i] *) => %24[i]
	Pt + (%25[i] *) => Label2PrimFld[i]
	Pt + (%26 *) => Label2PrimFldArr
	_Pt + (Label2Obj *) => LabelRef3
prim2RefArgTStub:
	prim2RefArgTStub => Prim2RefT
prim2RefF:
	prim2RefF => Prim2RefF
ref2PrimF:
	ref2PrimF => Ref2PrimF
Label2PrimFldArr:
	Label2PrimFldArr + (* Obj2RefT) => %21
	Label2PrimFldArr + (* Obj2PrimT) => Label2Prim
FptArr:
	_FptArr + (* Obj2RefT) => Obj2RefT
	_FptArr + (* Obj2PrimT) => Obj2PrimT
	FptArr + (Label2ObjX *) => Label2ObjX
SinkF2PrimF:
	SinkF2PrimF => SinkF2Prim
Src2Sink:
	Src2Sink => Flow3
loadStatPrimCtxt:
	_loadStatPrimCtxt[i] + (Label2PrimFldStat[i] *) => Label2Prim
Prim2PrimF:
	_Prim2PrimF + (%15 *) => SinkF2Prim
Prim2PrimT:
	Prim2PrimT + (Label2Prim *) => Label2Prim
storeStatPrimCtxt:
	_storeStatPrimCtxt[i] + (Label2Prim *) => Label2PrimFldStat[i]
Ref2PrimT:
	Ref2PrimT + (_Pt *) => Obj2PrimT
SinkF2Prim:
	_SinkF2Prim + (%17 *) => Src2Sink
loadPrimCtxtArr:
	_loadPrimCtxtArr + (%23 *) => Label2Prim
Ref2PrimF:
	_Ref2PrimF + (%11 *) => %12
sink2Label:
	sink2Label + (* Label2Obj) => %8
	sink2Label + (* Label2Prim) => %11
	sink2Label + (* Label2Obj) => %13
	sink2Label + (* Label2Prim) => %15
PostFlowsTo:
	_PostFlowsTo + (* _Transfer) => %1
assignPrimCtxt:
	_assignPrimCtxt + (Label2Prim *) => Label2Prim
fptArr:
	fptArr => FptArr
label2PrimT:
	label2PrimT => Label2PrimT
sinkF2PrimF:
	sinkF2PrimF => SinkF2PrimF
Store:
	Store[i] + (_Pt *) => %2[i]
	Store[i] + (_pt *) => %3[i]
	Store[i] + (_Pt *) => %4[i]
pt:
	pt + (%2[i] *) => Fpt[i]
	_pt + (* Store[i]) => %3[i]
	pt + (%5 *) => FptArr
	_pt + (* StoreArr) => %6
	pt => Pt
Label2RefT:
	Label2RefT + (* Pt) => Label2ObjT
transfer:
	transfer => Transfer
transferSelf:
	transferSelf => Transfer
sinkF2RefF:
	sinkF2RefF => SinkF2RefF
Label2PrimFldStat:
	Label2PrimFldStat[i] + (* _loadStatPrimCtxt[i]) => Label2Prim
ref2PrimTStub:
	ref2PrimTStub => Ref2PrimT
src2Label:
	src2Label + (* Label2Obj) => %16
	src2Label + (* Label2Prim) => %17
	src2Label + (* Label2PrimFld) => %18
Obj2PrimT:
	Obj2PrimT + (_FptArr *) => Obj2PrimT
	Obj2PrimT + (Label2Obj *) => Label2Prim
	Obj2PrimT + (Label2PrimFldArr *) => Label2Prim
loadPrimCtxt:
	_loadPrimCtxt + (%22 *) => Label2Prim
	_loadPrimCtxt[i] + (%24[i] *) => Label2Prim
Obj2RefT:
	Obj2RefT + (_FptArr *) => Obj2RefT
	Obj2RefT + (Label2Obj *) => %19
	Obj2RefT + (Label2PrimFldArr *) => %21
Prim2RefT:
	Prim2RefT + (Label2Prim *) => %20
prim2PrimF:
	prim2PrimF => Prim2PrimF
midFlowsTo:
	midFlowsTo => MidFlowsTo
Prim2RefF:
	_Prim2RefF + (%14 *) => SinkF2Prim
%13:
	%13 + (* _Pt) => %14
ref2RefF:
	ref2RefF => Ref2RefF
Transfer:
	Transfer + (PreFlowsTo *) => %0
	_Transfer + (_PostFlowsTo *) => %1
Fpt:
	Fpt + (Label2ObjT *) => Label2ObjT
	Fpt + (SinkF2Obj *) => SinkF2Obj
ref2RefArgTStub:
	ref2RefArgTStub => Ref2RefT
Label2PrimFld:
	Label2PrimFld + (src2Label *) => %18
	Label2PrimFld[i] + (* _Pt) => %24[i]
%24:
	%24[i] + (* _loadPrimCtxt[i]) => Label2Prim
%25:
	%25[i] + (* Pt) => Label2PrimFld[i]
%26:
	%26 + (* Pt) => Label2PrimFldArr
%20:
	%20 + (* Pt) => Label2ObjX
%21:
	%21 + (* Pt) => Label2ObjX
%22:
	%22 + (* _loadPrimCtxt) => Label2Prim
%23:
	%23 + (* _loadPrimCtxtArr) => Label2Prim
prim2PrimTStub:
	prim2PrimTStub => Prim2PrimT
fpt:
	fpt[i] => Fpt[i]
PreFlowsTo:
	PreFlowsTo + (* Transfer) => %0
	_PreFlowsTo + (%1 *) => Pt
ref2RefRetTStub:
	ref2RefRetTStub => Ref2RefT
SinkF2RefF:
	SinkF2RefF + (* Pt) => SinkF2Obj
prim2RefRetTStub:
	prim2RefRetTStub => Prim2RefT
%19:
	%19 + (* Pt) => Label2ObjX
%18:
	%18 + (* _SinkF2Obj) => Src2Sink
%11:
	%11 + (* _Ref2PrimF) => %12
%10:
	%10 + (* Pt) => SinkF2Obj
Label2Obj:
	Label2Obj + (sink2Label *) => %8
	Label2Obj + (sink2Label *) => %13
	Label2Obj + (src2Label *) => %16
	Label2Obj + (* Obj2RefT) => %19
	Label2Obj + (* Obj2PrimT) => Label2Prim
	Label2Obj + (* _Pt) => LabelRef3
%12:
	%12 + (* Pt) => SinkF2Obj
%15:
	%15 + (* _Prim2PrimF) => SinkF2Prim
%14:
	%14 + (* _Prim2RefF) => SinkF2Prim
%17:
	%17 + (* _SinkF2Prim) => Src2Sink
%16:
	%16 + (* _SinkF2Obj) => Src2Sink
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
  case 18:
  case 20:
  case 21:
  case 23:
  case 25:
  case 27:
  case 29:
  case 31:
  case 33:
  case 34:
  case 40:
  case 49:
  case 50:
  case 55:
  case 68:
  case 78:
  case 79:
  case 80:
  case 82:
  case 86:
  case 87:
  case 89:
  case 91:
    return true;
  default:
    return false;
  }
}

public int numKinds() {
  return 95;
}

public int symbolToKind(String symbol) {
  if (symbol.equals("Label2RefT")) return 0;
  if (symbol.equals("label2RefT")) return 1;
  if (symbol.equals("Label2PrimT")) return 2;
  if (symbol.equals("label2PrimT")) return 3;
  if (symbol.equals("SinkF2RefF")) return 4;
  if (symbol.equals("sinkF2RefF")) return 5;
  if (symbol.equals("SinkF2PrimF")) return 6;
  if (symbol.equals("sinkF2PrimF")) return 7;
  if (symbol.equals("Ref2RefF")) return 8;
  if (symbol.equals("ref2RefF")) return 9;
  if (symbol.equals("Ref2PrimF")) return 10;
  if (symbol.equals("ref2PrimF")) return 11;
  if (symbol.equals("Prim2RefF")) return 12;
  if (symbol.equals("prim2RefF")) return 13;
  if (symbol.equals("Prim2PrimF")) return 14;
  if (symbol.equals("prim2PrimF")) return 15;
  if (symbol.equals("Ref2RefT")) return 16;
  if (symbol.equals("ref2RefArgTStub")) return 17;
  if (symbol.equals("ref2RefRetTStub")) return 18;
  if (symbol.equals("Prim2RefT")) return 19;
  if (symbol.equals("prim2RefArgTStub")) return 20;
  if (symbol.equals("prim2RefRetTStub")) return 21;
  if (symbol.equals("Ref2PrimT")) return 22;
  if (symbol.equals("ref2PrimTStub")) return 23;
  if (symbol.equals("Prim2PrimT")) return 24;
  if (symbol.equals("prim2PrimTStub")) return 25;
  if (symbol.equals("PreFlowsTo")) return 26;
  if (symbol.equals("preFlowsTo")) return 27;
  if (symbol.equals("PostFlowsTo")) return 28;
  if (symbol.equals("postFlowsTo")) return 29;
  if (symbol.equals("MidFlowsTo")) return 30;
  if (symbol.equals("midFlowsTo")) return 31;
  if (symbol.equals("Transfer")) return 32;
  if (symbol.equals("transfer")) return 33;
  if (symbol.equals("transferSelf")) return 34;
  if (symbol.equals("%0")) return 35;
  if (symbol.equals("Pt")) return 36;
  if (symbol.equals("%1")) return 37;
  if (symbol.equals("Fpt")) return 38;
  if (symbol.equals("Store")) return 39;
  if (symbol.equals("pt")) return 40;
  if (symbol.equals("%2")) return 41;
  if (symbol.equals("%3")) return 42;
  if (symbol.equals("%4")) return 43;
  if (symbol.equals("FptArr")) return 44;
  if (symbol.equals("StoreArr")) return 45;
  if (symbol.equals("%5")) return 46;
  if (symbol.equals("%6")) return 47;
  if (symbol.equals("%7")) return 48;
  if (symbol.equals("fpt")) return 49;
  if (symbol.equals("fptArr")) return 50;
  if (symbol.equals("Obj2RefT")) return 51;
  if (symbol.equals("Obj2PrimT")) return 52;
  if (symbol.equals("Label2ObjT")) return 53;
  if (symbol.equals("SinkF2Obj")) return 54;
  if (symbol.equals("sink2Label")) return 55;
  if (symbol.equals("Label2Obj")) return 56;
  if (symbol.equals("%8")) return 57;
  if (symbol.equals("%9")) return 58;
  if (symbol.equals("%10")) return 59;
  if (symbol.equals("Label2Prim")) return 60;
  if (symbol.equals("%11")) return 61;
  if (symbol.equals("%12")) return 62;
  if (symbol.equals("SinkF2Prim")) return 63;
  if (symbol.equals("%13")) return 64;
  if (symbol.equals("%14")) return 65;
  if (symbol.equals("%15")) return 66;
  if (symbol.equals("Src2Sink")) return 67;
  if (symbol.equals("src2Label")) return 68;
  if (symbol.equals("%16")) return 69;
  if (symbol.equals("%17")) return 70;
  if (symbol.equals("Label2PrimFld")) return 71;
  if (symbol.equals("%18")) return 72;
  if (symbol.equals("Label2ObjX")) return 73;
  if (symbol.equals("%19")) return 74;
  if (symbol.equals("%20")) return 75;
  if (symbol.equals("Label2PrimFldArr")) return 76;
  if (symbol.equals("%21")) return 77;
  if (symbol.equals("assignPrimCtxt")) return 78;
  if (symbol.equals("assignPrimCCtxt")) return 79;
  if (symbol.equals("loadPrimCtxt")) return 80;
  if (symbol.equals("%22")) return 81;
  if (symbol.equals("loadPrimCtxtArr")) return 82;
  if (symbol.equals("%23")) return 83;
  if (symbol.equals("%24")) return 84;
  if (symbol.equals("Label2PrimFldStat")) return 85;
  if (symbol.equals("loadStatPrimCtxt")) return 86;
  if (symbol.equals("storePrimCtxt")) return 87;
  if (symbol.equals("%25")) return 88;
  if (symbol.equals("storePrimCtxtArr")) return 89;
  if (symbol.equals("%26")) return 90;
  if (symbol.equals("storeStatPrimCtxt")) return 91;
  if (symbol.equals("LabelRef3")) return 92;
  if (symbol.equals("LabelPrim3")) return 93;
  if (symbol.equals("Flow3")) return 94;
  throw new RuntimeException("Unknown symbol "+symbol);
}

public String kindToSymbol(int kind) {
  switch (kind) {
  case 0: return "Label2RefT";
  case 1: return "label2RefT";
  case 2: return "Label2PrimT";
  case 3: return "label2PrimT";
  case 4: return "SinkF2RefF";
  case 5: return "sinkF2RefF";
  case 6: return "SinkF2PrimF";
  case 7: return "sinkF2PrimF";
  case 8: return "Ref2RefF";
  case 9: return "ref2RefF";
  case 10: return "Ref2PrimF";
  case 11: return "ref2PrimF";
  case 12: return "Prim2RefF";
  case 13: return "prim2RefF";
  case 14: return "Prim2PrimF";
  case 15: return "prim2PrimF";
  case 16: return "Ref2RefT";
  case 17: return "ref2RefArgTStub";
  case 18: return "ref2RefRetTStub";
  case 19: return "Prim2RefT";
  case 20: return "prim2RefArgTStub";
  case 21: return "prim2RefRetTStub";
  case 22: return "Ref2PrimT";
  case 23: return "ref2PrimTStub";
  case 24: return "Prim2PrimT";
  case 25: return "prim2PrimTStub";
  case 26: return "PreFlowsTo";
  case 27: return "preFlowsTo";
  case 28: return "PostFlowsTo";
  case 29: return "postFlowsTo";
  case 30: return "MidFlowsTo";
  case 31: return "midFlowsTo";
  case 32: return "Transfer";
  case 33: return "transfer";
  case 34: return "transferSelf";
  case 35: return "%0";
  case 36: return "Pt";
  case 37: return "%1";
  case 38: return "Fpt";
  case 39: return "Store";
  case 40: return "pt";
  case 41: return "%2";
  case 42: return "%3";
  case 43: return "%4";
  case 44: return "FptArr";
  case 45: return "StoreArr";
  case 46: return "%5";
  case 47: return "%6";
  case 48: return "%7";
  case 49: return "fpt";
  case 50: return "fptArr";
  case 51: return "Obj2RefT";
  case 52: return "Obj2PrimT";
  case 53: return "Label2ObjT";
  case 54: return "SinkF2Obj";
  case 55: return "sink2Label";
  case 56: return "Label2Obj";
  case 57: return "%8";
  case 58: return "%9";
  case 59: return "%10";
  case 60: return "Label2Prim";
  case 61: return "%11";
  case 62: return "%12";
  case 63: return "SinkF2Prim";
  case 64: return "%13";
  case 65: return "%14";
  case 66: return "%15";
  case 67: return "Src2Sink";
  case 68: return "src2Label";
  case 69: return "%16";
  case 70: return "%17";
  case 71: return "Label2PrimFld";
  case 72: return "%18";
  case 73: return "Label2ObjX";
  case 74: return "%19";
  case 75: return "%20";
  case 76: return "Label2PrimFldArr";
  case 77: return "%21";
  case 78: return "assignPrimCtxt";
  case 79: return "assignPrimCCtxt";
  case 80: return "loadPrimCtxt";
  case 81: return "%22";
  case 82: return "loadPrimCtxtArr";
  case 83: return "%23";
  case 84: return "%24";
  case 85: return "Label2PrimFldStat";
  case 86: return "loadStatPrimCtxt";
  case 87: return "storePrimCtxt";
  case 88: return "%25";
  case 89: return "storePrimCtxtArr";
  case 90: return "%26";
  case 91: return "storeStatPrimCtxt";
  case 92: return "LabelRef3";
  case 93: return "LabelPrim3";
  case 94: return "Flow3";
  default: throw new RuntimeException("Unknown kind "+kind);
  }
}

public void process(Edge base) {
  switch (base.kind) {
  case 0: /* Label2RefT */
    /* Label2RefT + (* Pt) => Label2ObjT */
    for(Edge other : base.to.getOutEdges(36)){
      addEdge(base.from, other.to, 53, base, other, false);
    }
    break;
  case 1: /* label2RefT */
    /* label2RefT => Label2RefT */
    addEdge(base.from, base.to, 0, base, false);
    break;
  case 2: /* Label2PrimT */
    /* Label2PrimT => Label2Prim */
    addEdge(base.from, base.to, 60, base, false);
    break;
  case 3: /* label2PrimT */
    /* label2PrimT => Label2PrimT */
    addEdge(base.from, base.to, 2, base, false);
    break;
  case 4: /* SinkF2RefF */
    /* SinkF2RefF + (* Pt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(36)){
      addEdge(base.from, other.to, 54, base, other, false);
    }
    break;
  case 5: /* sinkF2RefF */
    /* sinkF2RefF => SinkF2RefF */
    addEdge(base.from, base.to, 4, base, false);
    break;
  case 6: /* SinkF2PrimF */
    /* SinkF2PrimF => SinkF2Prim */
    addEdge(base.from, base.to, 63, base, false);
    break;
  case 7: /* sinkF2PrimF */
    /* sinkF2PrimF => SinkF2PrimF */
    addEdge(base.from, base.to, 6, base, false);
    break;
  case 8: /* Ref2RefF */
    /* _Ref2RefF + (%9 *) => %10 */
    for(Edge other : base.to.getInEdges(58)){
      addEdge(other.from, base.from, 59, base, other, false);
    }
    break;
  case 9: /* ref2RefF */
    /* ref2RefF => Ref2RefF */
    addEdge(base.from, base.to, 8, base, false);
    break;
  case 10: /* Ref2PrimF */
    /* _Ref2PrimF + (%11 *) => %12 */
    for(Edge other : base.to.getInEdges(61)){
      addEdge(other.from, base.from, 62, base, other, false);
    }
    break;
  case 11: /* ref2PrimF */
    /* ref2PrimF => Ref2PrimF */
    addEdge(base.from, base.to, 10, base, false);
    break;
  case 12: /* Prim2RefF */
    /* _Prim2RefF + (%14 *) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(65)){
      addEdge(other.from, base.from, 63, base, other, false);
    }
    break;
  case 13: /* prim2RefF */
    /* prim2RefF => Prim2RefF */
    addEdge(base.from, base.to, 12, base, false);
    break;
  case 14: /* Prim2PrimF */
    /* _Prim2PrimF + (%15 *) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(66)){
      addEdge(other.from, base.from, 63, base, other, false);
    }
    break;
  case 15: /* prim2PrimF */
    /* prim2PrimF => Prim2PrimF */
    addEdge(base.from, base.to, 14, base, false);
    break;
  case 16: /* Ref2RefT */
    /* Ref2RefT + (_Pt *) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(36)){
      addEdge(other.to, base.to, 51, base, other, false);
    }
    break;
  case 17: /* ref2RefArgTStub */
    /* ref2RefArgTStub => Ref2RefT */
    addEdge(base.from, base.to, 16, base, false);
    break;
  case 18: /* ref2RefRetTStub */
    /* ref2RefRetTStub => Ref2RefT */
    addEdge(base.from, base.to, 16, base, false);
    break;
  case 19: /* Prim2RefT */
    /* Prim2RefT + (Label2Prim *) => %20 */
    for(Edge other : base.from.getInEdges(60)){
      addEdge(other.from, base.to, 75, base, other, false);
    }
    break;
  case 20: /* prim2RefArgTStub */
    /* prim2RefArgTStub => Prim2RefT */
    addEdge(base.from, base.to, 19, base, false);
    break;
  case 21: /* prim2RefRetTStub */
    /* prim2RefRetTStub => Prim2RefT */
    addEdge(base.from, base.to, 19, base, false);
    break;
  case 22: /* Ref2PrimT */
    /* Ref2PrimT + (_Pt *) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(36)){
      addEdge(other.to, base.to, 52, base, other, false);
    }
    break;
  case 23: /* ref2PrimTStub */
    /* ref2PrimTStub => Ref2PrimT */
    addEdge(base.from, base.to, 22, base, false);
    break;
  case 24: /* Prim2PrimT */
    /* Prim2PrimT + (Label2Prim *) => Label2Prim */
    for(Edge other : base.from.getInEdges(60)){
      addEdge(other.from, base.to, 60, base, other, false);
    }
    break;
  case 25: /* prim2PrimTStub */
    /* prim2PrimTStub => Prim2PrimT */
    addEdge(base.from, base.to, 24, base, false);
    break;
  case 26: /* PreFlowsTo */
    /* PreFlowsTo + (* Transfer) => %0 */
    for(Edge other : base.to.getOutEdges(32)){
      addEdge(base.from, other.to, 35, base, other, false);
    }
    /* _PreFlowsTo + (%1 *) => Pt */
    for(Edge other : base.to.getInEdges(37)){
      addEdge(other.from, base.from, 36, base, other, false);
    }
    break;
  case 27: /* preFlowsTo */
    /* preFlowsTo => PreFlowsTo */
    addEdge(base.from, base.to, 26, base, false);
    break;
  case 28: /* PostFlowsTo */
    /* _PostFlowsTo + (* _Transfer) => %1 */
    for(Edge other : base.from.getInEdges(32)){
      addEdge(base.to, other.from, 37, base, other, false);
    }
    break;
  case 29: /* postFlowsTo */
    /* postFlowsTo => PostFlowsTo */
    addEdge(base.from, base.to, 28, base, false);
    break;
  case 30: /* MidFlowsTo */
    /* MidFlowsTo + (%0 *) => PreFlowsTo */
    for(Edge other : base.from.getInEdges(35)){
      addEdge(other.from, base.to, 26, base, other, false);
    }
    break;
  case 31: /* midFlowsTo */
    /* midFlowsTo => MidFlowsTo */
    addEdge(base.from, base.to, 30, base, false);
    break;
  case 32: /* Transfer */
    /* Transfer + (PreFlowsTo *) => %0 */
    for(Edge other : base.from.getInEdges(26)){
      addEdge(other.from, base.to, 35, base, other, false);
    }
    /* _Transfer + (_PostFlowsTo *) => %1 */
    for(Edge other : base.to.getOutEdges(28)){
      addEdge(other.to, base.from, 37, base, other, false);
    }
    break;
  case 33: /* transfer */
    /* transfer => Transfer */
    addEdge(base.from, base.to, 32, base, false);
    break;
  case 34: /* transferSelf */
    /* transferSelf => Transfer */
    addEdge(base.from, base.to, 32, base, false);
    break;
  case 35: /* %0 */
    /* %0 + (* MidFlowsTo) => PreFlowsTo */
    for(Edge other : base.to.getOutEdges(30)){
      addEdge(base.from, other.to, 26, base, other, false);
    }
    break;
  case 36: /* Pt */
    /* _Pt + (* Store[i]) => %2[i] */
    for(Edge other : base.from.getOutEdges(39)){
      addEdge(base.to, other.to, 41, base, other, true);
    }
    /* Pt + (%3[i] *) => Fpt[i] */
    for(Edge other : base.from.getInEdges(42)){
      addEdge(other.from, base.to, 38, base, other, true);
    }
    /* _Pt + (* Store[i]) => %4[i] */
    for(Edge other : base.from.getOutEdges(39)){
      addEdge(base.to, other.to, 43, base, other, true);
    }
    /* Pt + (%4[i] *) => Fpt[i] */
    for(Edge other : base.from.getInEdges(43)){
      addEdge(other.from, base.to, 38, base, other, true);
    }
    /* _Pt + (* StoreArr) => %5 */
    for(Edge other : base.from.getOutEdges(45)){
      addEdge(base.to, other.to, 46, base, other, false);
    }
    /* Pt + (%6 *) => FptArr */
    for(Edge other : base.from.getInEdges(47)){
      addEdge(other.from, base.to, 44, base, other, false);
    }
    /* _Pt + (* StoreArr) => %7 */
    for(Edge other : base.from.getOutEdges(45)){
      addEdge(base.to, other.to, 48, base, other, false);
    }
    /* Pt + (%7 *) => FptArr */
    for(Edge other : base.from.getInEdges(48)){
      addEdge(other.from, base.to, 44, base, other, false);
    }
    /* _Pt + (* Ref2RefT) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(16)){
      addEdge(base.to, other.to, 51, base, other, false);
    }
    /* _Pt + (* Ref2PrimT) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(22)){
      addEdge(base.to, other.to, 52, base, other, false);
    }
    /* Pt + (Label2RefT *) => Label2ObjT */
    for(Edge other : base.from.getInEdges(0)){
      addEdge(other.from, base.to, 53, base, other, false);
    }
    /* Pt + (SinkF2RefF *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(4)){
      addEdge(other.from, base.to, 54, base, other, false);
    }
    /* _Pt + (%8 *) => %9 */
    for(Edge other : base.to.getInEdges(57)){
      addEdge(other.from, base.from, 58, base, other, false);
    }
    /* Pt + (%10 *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(59)){
      addEdge(other.from, base.to, 54, base, other, false);
    }
    /* Pt + (%12 *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(62)){
      addEdge(other.from, base.to, 54, base, other, false);
    }
    /* _Pt + (%13 *) => %14 */
    for(Edge other : base.to.getInEdges(64)){
      addEdge(other.from, base.from, 65, base, other, false);
    }
    /* Pt + (%19 *) => Label2ObjX */
    for(Edge other : base.from.getInEdges(74)){
      addEdge(other.from, base.to, 73, base, other, false);
    }
    /* Pt + (%20 *) => Label2ObjX */
    for(Edge other : base.from.getInEdges(75)){
      addEdge(other.from, base.to, 73, base, other, false);
    }
    /* Pt + (%21 *) => Label2ObjX */
    for(Edge other : base.from.getInEdges(77)){
      addEdge(other.from, base.to, 73, base, other, false);
    }
    /* _Pt + (Label2ObjT *) => %22 */
    for(Edge other : base.to.getInEdges(53)){
      addEdge(other.from, base.from, 81, base, other, false);
    }
    /* _Pt + (Label2ObjX *) => %23 */
    for(Edge other : base.to.getInEdges(73)){
      addEdge(other.from, base.from, 83, base, other, false);
    }
    /* _Pt + (Label2PrimFld[i] *) => %24[i] */
    for(Edge other : base.to.getInEdges(71)){
      addEdge(other.from, base.from, 84, base, other, true);
    }
    /* Pt + (%25[i] *) => Label2PrimFld[i] */
    for(Edge other : base.from.getInEdges(88)){
      addEdge(other.from, base.to, 71, base, other, true);
    }
    /* Pt + (%26 *) => Label2PrimFldArr */
    for(Edge other : base.from.getInEdges(90)){
      addEdge(other.from, base.to, 76, base, other, false);
    }
    /* _Pt + (Label2Obj *) => LabelRef3 */
    for(Edge other : base.to.getInEdges(56)){
      addEdge(other.from, base.from, 92, base, other, false);
    }
    break;
  case 37: /* %1 */
    /* %1 + (* _PreFlowsTo) => Pt */
    for(Edge other : base.to.getInEdges(26)){
      addEdge(base.from, other.from, 36, base, other, false);
    }
    break;
  case 38: /* Fpt */
    /* Fpt + (Label2ObjT *) => Label2ObjT */
    for(Edge other : base.from.getInEdges(53)){
      addEdge(other.from, base.to, 53, base, other, false);
    }
    /* Fpt + (SinkF2Obj *) => SinkF2Obj */
    for(Edge other : base.from.getInEdges(54)){
      addEdge(other.from, base.to, 54, base, other, false);
    }
    break;
  case 39: /* Store */
    /* Store[i] + (_Pt *) => %2[i] */
    for(Edge other : base.from.getOutEdges(36)){
      addEdge(other.to, base.to, 41, base, other, true);
    }
    /* Store[i] + (_pt *) => %3[i] */
    for(Edge other : base.from.getOutEdges(40)){
      addEdge(other.to, base.to, 42, base, other, true);
    }
    /* Store[i] + (_Pt *) => %4[i] */
    for(Edge other : base.from.getOutEdges(36)){
      addEdge(other.to, base.to, 43, base, other, true);
    }
    break;
  case 40: /* pt */
    /* pt + (%2[i] *) => Fpt[i] */
    for(Edge other : base.from.getInEdges(41)){
      addEdge(other.from, base.to, 38, base, other, true);
    }
    /* _pt + (* Store[i]) => %3[i] */
    for(Edge other : base.from.getOutEdges(39)){
      addEdge(base.to, other.to, 42, base, other, true);
    }
    /* pt + (%5 *) => FptArr */
    for(Edge other : base.from.getInEdges(46)){
      addEdge(other.from, base.to, 44, base, other, false);
    }
    /* _pt + (* StoreArr) => %6 */
    for(Edge other : base.from.getOutEdges(45)){
      addEdge(base.to, other.to, 47, base, other, false);
    }
    /* pt => Pt */
    addEdge(base.from, base.to, 36, base, false);
    break;
  case 41: /* %2 */
    /* %2[i] + (* pt) => Fpt[i] */
    for(Edge other : base.to.getOutEdges(40)){
      addEdge(base.from, other.to, 38, base, other, true);
    }
    break;
  case 42: /* %3 */
    /* %3[i] + (* Pt) => Fpt[i] */
    for(Edge other : base.to.getOutEdges(36)){
      addEdge(base.from, other.to, 38, base, other, true);
    }
    break;
  case 43: /* %4 */
    /* %4[i] + (* Pt) => Fpt[i] */
    for(Edge other : base.to.getOutEdges(36)){
      addEdge(base.from, other.to, 38, base, other, true);
    }
    break;
  case 44: /* FptArr */
    /* _FptArr + (* Obj2RefT) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(51)){
      addEdge(base.to, other.to, 51, base, other, false);
    }
    /* _FptArr + (* Obj2PrimT) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(52)){
      addEdge(base.to, other.to, 52, base, other, false);
    }
    /* FptArr + (Label2ObjX *) => Label2ObjX */
    for(Edge other : base.from.getInEdges(73)){
      addEdge(other.from, base.to, 73, base, other, false);
    }
    break;
  case 45: /* StoreArr */
    /* StoreArr + (_Pt *) => %5 */
    for(Edge other : base.from.getOutEdges(36)){
      addEdge(other.to, base.to, 46, base, other, false);
    }
    /* StoreArr + (_pt *) => %6 */
    for(Edge other : base.from.getOutEdges(40)){
      addEdge(other.to, base.to, 47, base, other, false);
    }
    /* StoreArr + (_Pt *) => %7 */
    for(Edge other : base.from.getOutEdges(36)){
      addEdge(other.to, base.to, 48, base, other, false);
    }
    break;
  case 46: /* %5 */
    /* %5 + (* pt) => FptArr */
    for(Edge other : base.to.getOutEdges(40)){
      addEdge(base.from, other.to, 44, base, other, false);
    }
    break;
  case 47: /* %6 */
    /* %6 + (* Pt) => FptArr */
    for(Edge other : base.to.getOutEdges(36)){
      addEdge(base.from, other.to, 44, base, other, false);
    }
    break;
  case 48: /* %7 */
    /* %7 + (* Pt) => FptArr */
    for(Edge other : base.to.getOutEdges(36)){
      addEdge(base.from, other.to, 44, base, other, false);
    }
    break;
  case 49: /* fpt */
    /* fpt[i] => Fpt[i] */
    addEdge(base.from, base.to, 38, base, true);
    break;
  case 50: /* fptArr */
    /* fptArr => FptArr */
    addEdge(base.from, base.to, 44, base, false);
    break;
  case 51: /* Obj2RefT */
    /* Obj2RefT + (_FptArr *) => Obj2RefT */
    for(Edge other : base.from.getOutEdges(44)){
      addEdge(other.to, base.to, 51, base, other, false);
    }
    /* Obj2RefT + (Label2Obj *) => %19 */
    for(Edge other : base.from.getInEdges(56)){
      addEdge(other.from, base.to, 74, base, other, false);
    }
    /* Obj2RefT + (Label2PrimFldArr *) => %21 */
    for(Edge other : base.from.getInEdges(76)){
      addEdge(other.from, base.to, 77, base, other, false);
    }
    break;
  case 52: /* Obj2PrimT */
    /* Obj2PrimT + (_FptArr *) => Obj2PrimT */
    for(Edge other : base.from.getOutEdges(44)){
      addEdge(other.to, base.to, 52, base, other, false);
    }
    /* Obj2PrimT + (Label2Obj *) => Label2Prim */
    for(Edge other : base.from.getInEdges(56)){
      addEdge(other.from, base.to, 60, base, other, false);
    }
    /* Obj2PrimT + (Label2PrimFldArr *) => Label2Prim */
    for(Edge other : base.from.getInEdges(76)){
      addEdge(other.from, base.to, 60, base, other, false);
    }
    break;
  case 53: /* Label2ObjT */
    /* Label2ObjT + (* Fpt) => Label2ObjT */
    for(Edge other : base.to.getOutEdges(38)){
      addEdge(base.from, other.to, 53, base, other, false);
    }
    /* Label2ObjT => Label2Obj */
    addEdge(base.from, base.to, 56, base, false);
    /* Label2ObjT + (* _Pt) => %22 */
    for(Edge other : base.to.getInEdges(36)){
      addEdge(base.from, other.from, 81, base, other, false);
    }
    break;
  case 54: /* SinkF2Obj */
    /* SinkF2Obj + (* Fpt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(38)){
      addEdge(base.from, other.to, 54, base, other, false);
    }
    /* _SinkF2Obj + (%16 *) => Src2Sink */
    for(Edge other : base.to.getInEdges(69)){
      addEdge(other.from, base.from, 67, base, other, false);
    }
    /* _SinkF2Obj + (%18 *) => Src2Sink */
    for(Edge other : base.to.getInEdges(72)){
      addEdge(other.from, base.from, 67, base, other, false);
    }
    break;
  case 55: /* sink2Label */
    /* sink2Label + (* Label2Obj) => %8 */
    for(Edge other : base.to.getOutEdges(56)){
      addEdge(base.from, other.to, 57, base, other, false);
    }
    /* sink2Label + (* Label2Prim) => %11 */
    for(Edge other : base.to.getOutEdges(60)){
      addEdge(base.from, other.to, 61, base, other, false);
    }
    /* sink2Label + (* Label2Obj) => %13 */
    for(Edge other : base.to.getOutEdges(56)){
      addEdge(base.from, other.to, 64, base, other, false);
    }
    /* sink2Label + (* Label2Prim) => %15 */
    for(Edge other : base.to.getOutEdges(60)){
      addEdge(base.from, other.to, 66, base, other, false);
    }
    break;
  case 56: /* Label2Obj */
    /* Label2Obj + (sink2Label *) => %8 */
    for(Edge other : base.from.getInEdges(55)){
      addEdge(other.from, base.to, 57, base, other, false);
    }
    /* Label2Obj + (sink2Label *) => %13 */
    for(Edge other : base.from.getInEdges(55)){
      addEdge(other.from, base.to, 64, base, other, false);
    }
    /* Label2Obj + (src2Label *) => %16 */
    for(Edge other : base.from.getInEdges(68)){
      addEdge(other.from, base.to, 69, base, other, false);
    }
    /* Label2Obj + (* Obj2RefT) => %19 */
    for(Edge other : base.to.getOutEdges(51)){
      addEdge(base.from, other.to, 74, base, other, false);
    }
    /* Label2Obj + (* Obj2PrimT) => Label2Prim */
    for(Edge other : base.to.getOutEdges(52)){
      addEdge(base.from, other.to, 60, base, other, false);
    }
    /* Label2Obj + (* _Pt) => LabelRef3 */
    for(Edge other : base.to.getInEdges(36)){
      addEdge(base.from, other.from, 92, base, other, false);
    }
    break;
  case 57: /* %8 */
    /* %8 + (* _Pt) => %9 */
    for(Edge other : base.to.getInEdges(36)){
      addEdge(base.from, other.from, 58, base, other, false);
    }
    break;
  case 58: /* %9 */
    /* %9 + (* _Ref2RefF) => %10 */
    for(Edge other : base.to.getInEdges(8)){
      addEdge(base.from, other.from, 59, base, other, false);
    }
    break;
  case 59: /* %10 */
    /* %10 + (* Pt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(36)){
      addEdge(base.from, other.to, 54, base, other, false);
    }
    break;
  case 60: /* Label2Prim */
    /* Label2Prim + (sink2Label *) => %11 */
    for(Edge other : base.from.getInEdges(55)){
      addEdge(other.from, base.to, 61, base, other, false);
    }
    /* Label2Prim + (sink2Label *) => %15 */
    for(Edge other : base.from.getInEdges(55)){
      addEdge(other.from, base.to, 66, base, other, false);
    }
    /* Label2Prim + (src2Label *) => %17 */
    for(Edge other : base.from.getInEdges(68)){
      addEdge(other.from, base.to, 70, base, other, false);
    }
    /* Label2Prim + (* Prim2RefT) => %20 */
    for(Edge other : base.to.getOutEdges(19)){
      addEdge(base.from, other.to, 75, base, other, false);
    }
    /* Label2Prim + (* _assignPrimCtxt) => Label2Prim */
    for(Edge other : base.to.getInEdges(78)){
      addEdge(base.from, other.from, 60, base, other, false);
    }
    /* Label2Prim + (* _assignPrimCCtxt) => Label2Prim */
    for(Edge other : base.to.getInEdges(79)){
      addEdge(base.from, other.from, 60, base, other, false);
    }
    /* Label2Prim + (* Prim2PrimT) => Label2Prim */
    for(Edge other : base.to.getOutEdges(24)){
      addEdge(base.from, other.to, 60, base, other, false);
    }
    /* Label2Prim + (* _storePrimCtxt[i]) => %25[i] */
    for(Edge other : base.to.getInEdges(87)){
      addEdge(base.from, other.from, 88, base, other, true);
    }
    /* Label2Prim + (* _storePrimCtxtArr) => %26 */
    for(Edge other : base.to.getInEdges(89)){
      addEdge(base.from, other.from, 90, base, other, false);
    }
    /* Label2Prim + (* _storeStatPrimCtxt[i]) => Label2PrimFldStat[i] */
    for(Edge other : base.to.getInEdges(91)){
      addEdge(base.from, other.from, 85, base, other, true);
    }
    /* Label2Prim => LabelPrim3 */
    addEdge(base.from, base.to, 93, base, false);
    break;
  case 61: /* %11 */
    /* %11 + (* _Ref2PrimF) => %12 */
    for(Edge other : base.to.getInEdges(10)){
      addEdge(base.from, other.from, 62, base, other, false);
    }
    break;
  case 62: /* %12 */
    /* %12 + (* Pt) => SinkF2Obj */
    for(Edge other : base.to.getOutEdges(36)){
      addEdge(base.from, other.to, 54, base, other, false);
    }
    break;
  case 63: /* SinkF2Prim */
    /* _SinkF2Prim + (%17 *) => Src2Sink */
    for(Edge other : base.to.getInEdges(70)){
      addEdge(other.from, base.from, 67, base, other, false);
    }
    break;
  case 64: /* %13 */
    /* %13 + (* _Pt) => %14 */
    for(Edge other : base.to.getInEdges(36)){
      addEdge(base.from, other.from, 65, base, other, false);
    }
    break;
  case 65: /* %14 */
    /* %14 + (* _Prim2RefF) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(12)){
      addEdge(base.from, other.from, 63, base, other, false);
    }
    break;
  case 66: /* %15 */
    /* %15 + (* _Prim2PrimF) => SinkF2Prim */
    for(Edge other : base.to.getInEdges(14)){
      addEdge(base.from, other.from, 63, base, other, false);
    }
    break;
  case 67: /* Src2Sink */
    /* Src2Sink => Flow3 */
    addEdge(base.from, base.to, 94, base, false);
    break;
  case 68: /* src2Label */
    /* src2Label + (* Label2Obj) => %16 */
    for(Edge other : base.to.getOutEdges(56)){
      addEdge(base.from, other.to, 69, base, other, false);
    }
    /* src2Label + (* Label2Prim) => %17 */
    for(Edge other : base.to.getOutEdges(60)){
      addEdge(base.from, other.to, 70, base, other, false);
    }
    /* src2Label + (* Label2PrimFld) => %18 */
    for(Edge other : base.to.getOutEdges(71)){
      addEdge(base.from, other.to, 72, base, other, false);
    }
    break;
  case 69: /* %16 */
    /* %16 + (* _SinkF2Obj) => Src2Sink */
    for(Edge other : base.to.getInEdges(54)){
      addEdge(base.from, other.from, 67, base, other, false);
    }
    break;
  case 70: /* %17 */
    /* %17 + (* _SinkF2Prim) => Src2Sink */
    for(Edge other : base.to.getInEdges(63)){
      addEdge(base.from, other.from, 67, base, other, false);
    }
    break;
  case 71: /* Label2PrimFld */
    /* Label2PrimFld + (src2Label *) => %18 */
    for(Edge other : base.from.getInEdges(68)){
      addEdge(other.from, base.to, 72, base, other, false);
    }
    /* Label2PrimFld[i] + (* _Pt) => %24[i] */
    for(Edge other : base.to.getInEdges(36)){
      addEdge(base.from, other.from, 84, base, other, true);
    }
    break;
  case 72: /* %18 */
    /* %18 + (* _SinkF2Obj) => Src2Sink */
    for(Edge other : base.to.getInEdges(54)){
      addEdge(base.from, other.from, 67, base, other, false);
    }
    break;
  case 73: /* Label2ObjX */
    /* Label2ObjX => Label2Obj */
    addEdge(base.from, base.to, 56, base, false);
    /* Label2ObjX + (* FptArr) => Label2ObjX */
    for(Edge other : base.to.getOutEdges(44)){
      addEdge(base.from, other.to, 73, base, other, false);
    }
    /* Label2ObjX + (* _Pt) => %23 */
    for(Edge other : base.to.getInEdges(36)){
      addEdge(base.from, other.from, 83, base, other, false);
    }
    break;
  case 74: /* %19 */
    /* %19 + (* Pt) => Label2ObjX */
    for(Edge other : base.to.getOutEdges(36)){
      addEdge(base.from, other.to, 73, base, other, false);
    }
    break;
  case 75: /* %20 */
    /* %20 + (* Pt) => Label2ObjX */
    for(Edge other : base.to.getOutEdges(36)){
      addEdge(base.from, other.to, 73, base, other, false);
    }
    break;
  case 76: /* Label2PrimFldArr */
    /* Label2PrimFldArr + (* Obj2RefT) => %21 */
    for(Edge other : base.to.getOutEdges(51)){
      addEdge(base.from, other.to, 77, base, other, false);
    }
    /* Label2PrimFldArr + (* Obj2PrimT) => Label2Prim */
    for(Edge other : base.to.getOutEdges(52)){
      addEdge(base.from, other.to, 60, base, other, false);
    }
    break;
  case 77: /* %21 */
    /* %21 + (* Pt) => Label2ObjX */
    for(Edge other : base.to.getOutEdges(36)){
      addEdge(base.from, other.to, 73, base, other, false);
    }
    break;
  case 78: /* assignPrimCtxt */
    /* _assignPrimCtxt + (Label2Prim *) => Label2Prim */
    for(Edge other : base.to.getInEdges(60)){
      addEdge(other.from, base.from, 60, base, other, false);
    }
    break;
  case 79: /* assignPrimCCtxt */
    /* _assignPrimCCtxt + (Label2Prim *) => Label2Prim */
    for(Edge other : base.to.getInEdges(60)){
      addEdge(other.from, base.from, 60, base, other, false);
    }
    break;
  case 80: /* loadPrimCtxt */
    /* _loadPrimCtxt + (%22 *) => Label2Prim */
    for(Edge other : base.to.getInEdges(81)){
      addEdge(other.from, base.from, 60, base, other, false);
    }
    /* _loadPrimCtxt[i] + (%24[i] *) => Label2Prim */
    for(Edge other : base.to.getInEdges(84)){
      addEdge(other.from, base.from, 60, base, other, false);
    }
    break;
  case 81: /* %22 */
    /* %22 + (* _loadPrimCtxt) => Label2Prim */
    for(Edge other : base.to.getInEdges(80)){
      addEdge(base.from, other.from, 60, base, other, false);
    }
    break;
  case 82: /* loadPrimCtxtArr */
    /* _loadPrimCtxtArr + (%23 *) => Label2Prim */
    for(Edge other : base.to.getInEdges(83)){
      addEdge(other.from, base.from, 60, base, other, false);
    }
    break;
  case 83: /* %23 */
    /* %23 + (* _loadPrimCtxtArr) => Label2Prim */
    for(Edge other : base.to.getInEdges(82)){
      addEdge(base.from, other.from, 60, base, other, false);
    }
    break;
  case 84: /* %24 */
    /* %24[i] + (* _loadPrimCtxt[i]) => Label2Prim */
    for(Edge other : base.to.getInEdges(80)){
      addEdge(base.from, other.from, 60, base, other, false);
    }
    break;
  case 85: /* Label2PrimFldStat */
    /* Label2PrimFldStat[i] + (* _loadStatPrimCtxt[i]) => Label2Prim */
    for(Edge other : base.to.getInEdges(86)){
      addEdge(base.from, other.from, 60, base, other, false);
    }
    break;
  case 86: /* loadStatPrimCtxt */
    /* _loadStatPrimCtxt[i] + (Label2PrimFldStat[i] *) => Label2Prim */
    for(Edge other : base.to.getInEdges(85)){
      addEdge(other.from, base.from, 60, base, other, false);
    }
    break;
  case 87: /* storePrimCtxt */
    /* _storePrimCtxt[i] + (Label2Prim *) => %25[i] */
    for(Edge other : base.to.getInEdges(60)){
      addEdge(other.from, base.from, 88, base, other, true);
    }
    break;
  case 88: /* %25 */
    /* %25[i] + (* Pt) => Label2PrimFld[i] */
    for(Edge other : base.to.getOutEdges(36)){
      addEdge(base.from, other.to, 71, base, other, true);
    }
    break;
  case 89: /* storePrimCtxtArr */
    /* _storePrimCtxtArr + (Label2Prim *) => %26 */
    for(Edge other : base.to.getInEdges(60)){
      addEdge(other.from, base.from, 90, base, other, false);
    }
    break;
  case 90: /* %26 */
    /* %26 + (* Pt) => Label2PrimFldArr */
    for(Edge other : base.to.getOutEdges(36)){
      addEdge(base.from, other.to, 76, base, other, false);
    }
    break;
  case 91: /* storeStatPrimCtxt */
    /* _storeStatPrimCtxt[i] + (Label2Prim *) => Label2PrimFldStat[i] */
    for(Edge other : base.to.getInEdges(60)){
      addEdge(other.from, base.from, 85, base, other, true);
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
  case 17:
    return (short)1;
  case 18:
    return (short)1;
  case 20:
    return (short)1;
  case 21:
    return (short)1;
  case 23:
    return (short)1;
  case 33:
    return (short)1;
  default:
    return (short)0;
  }
}

public boolean useReps() { return false; }

}