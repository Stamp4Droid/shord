#
# An algorithm for computing the loss of X given S and F
# X - a vectorized version of the matrix factorization model M = U'*V,
# e.g. X = [U(:); V(:)] where U is r*n, V is r*m, and M = U'*V
# S - an n*m matrix representing the number of successful trials for each (i,j) pair
# F - an n*m matrix representing the number of failed trials for each (i,j) pair
#
# Computes the log loss L, and its gradient dLU and dLV with respect to
# U and V respectively, with dl = [collect(dLU); collect(dLV)]
#
# Here, X = [collect(U); collect(V)] where U is d*n and V is d*m
#

module LogisticLoss

export LogisticLossFunction, LogisticLossGradient

#
# Reshapes X to get U and V, and computes M = U'*V
#
function GetDecomposition(X,S,F)

## STEP 0: Get some parameters
(n,m) = size(S)
r = convert(Int64, length(X)/(n+m))

## STEP 1: Reshape the argument X to get U and V
X = reshape(X,r,n+m)
U = X[:,1:n]
V = X[:,(n+1):(n+m)]

## STEP 2: Multiply U and V to get M
M = U'*V

return (M,U,V)

end

#
# Returns the logistic loss 
#
function LogisticLossFunction(X,S,F)

## STEP 0: Get the decomposition
(M,U,V) = GetDecomposition(X,S,F)

## STEP 1: Compute the negative log likelihood
return sum(sum(S.*log(1+exp(-M)) + F.*log(1+exp(M))))

end

#
# Returns the gradient of the logistic loss
#
function LogisticLossGradient(X,S,F)

## STEP 0: Get the decomposition
(M,U,V) = GetDecomposition(X,S,F)

## STEP 1: Compute the gradient
dLU = V*(-S.*exp(-M)./(1+exp(-M)))' + V*(F.*exp(M)./(1+exp(M)))'
dLV = U*(-S.*exp(-M)./(1+exp(-M))) + U*(F.*exp(M)./(1+exp(M)))

## STEP 2: Unroll the gradient
return [collect(dLU); collect(dLV)]

end

end
