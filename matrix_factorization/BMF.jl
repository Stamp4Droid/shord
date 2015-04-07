#
# Returns estimated decomposition [U,V].
# Here:
#   M = U'*V;
#   M[i][j] = p_ij
#
module BMF

using Optim
using LogisticLoss
importall LogisticLoss

export ComputeBMF

function ComputeBMF(S,F,r)

## STEP 0: Setup
println("Setup")

E = ((S+F) .!= 0);
M_E = (S-F)./(S+F);
M_E[!E] = 0;

M_E = sparse(M_E);
(n,m) = size(M_E);

rescal_param = sqrt(nnz(E)*r/vecnorm(M_E)^2);
if rescal_param != Inf
    M_E = rescal_param*M_E;
end

## STEP 1: Trimming
println("Trimming");

M_Et = M_E;
d = sum(E,1);
d_ = mean(full(d));
for col=1:m
    if(sum(E[:,col]) > 2*d_)
        list = find(E[:,col] > 0);
        p = randperm(length(list));
        M_Et[list(p[ceil(2*d_):end]),col] = 0;
    end
end

d = sum(E,2);
d_= mean(full(d));
for row=1:n
    if(sum(E[row,:]) > 2*d_)
        list = find(E[row,:] > 0);
        p = randperm(length(list));
        M_Et[row,list(p[ceil(2*d_):end])] = 0;
    end
end

## STEP 2: Sparse SVD
println("Sparse SVD");
(U0,S0vec,V0) = svds(M_Et, nsv=r); # M_Et = U0*S0*V0'
S0 = diagm(S0vec);

M_Et = 0;

## STEP 3: Initial Guess
println("Initial Guess");

S0 = S0*(m*n/nnz(E));
S0 = sqrt(S0);
U0 = (U0*S0)';
V0 = S0*V0';
X0 = [collect(U0); collect(V0)];

## STEP 4: Optimization
println("Optimization");

#options.Method = 'lbfgs'; options.maxIter = 400;

#X1 = minFunc(@(X) LogisticLoss(X,S,F), X0, options);
X1 = optimize(Xt -> LogisticLossFunction(Xt,S,F), X0).minimum;

## STEP 5: Return
println("Return");

if(rescal_param != Inf)
    X1 = X1/rescal_param;
end
X1 = reshape(X1,r,n+m);
U = X1[:,1:n];
V = X1[:,(n+1):(n+m)];

return (U,V);

end

end
