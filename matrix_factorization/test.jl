# printing
for x in 0:1; println(x); end

# optimization
using Optim
function foo(x::Vector)
	 return (1 - x[1])^2 + (2 - x[2])^2
end
println(optimize(foo, [0.0,0.0]))

# logistic loss part 1: matrix unrolling/reshaping
U = [0 1 2 3;7 8 9 10]
V = [4 5 6;11 12 13]
X = [collect(U); collect(V)]
X = reshape(X, 2, 7)
U = X[:,1:4]
V = X[:,5:7]

# logistic loss part 2: compute loss and gradient
using LogisticLoss
importall LogisticLoss
X = [collect(U); collect(V)]
S = [0 1 1;0 0 1;0 0 0;0 1 1]
F = [1 1 1;1 1 1;0 1 0;1 0 0]
println(LogisticLossFunction(X,S,F))
println(LogisticLossGradient(X,S,F))
