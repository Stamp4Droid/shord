function [U,V] = BMF(S,F,r)

% M = U'*V;
% M[i][j] = p_ij

addpath minFunc;

%% STEP 0: Setup
fprintf(1,'Setup\n');

E = (S+F) ~= 0;
M_E = (S-F)./(S+F);
M_E(~E) = 0;

M_E = sparse(M_E);
[n m] = size(M_E);

rescal_param = sqrt(nnz(E)*r/norm(M_E,'fro')^2) ;
if rescal_param ~= Inf
    M_E = rescal_param*M_E;
end

%% STEP 1: Trimming
fprintf(1,'Trimming\n');

M_Et = M_E;
d = sum(E);
d_ = mean(full(d));
for col=1:m
    if ( sum(E(:,col))>2*d_ )
        list = find( E(:,col) > 0 );
        p = randperm(length(list));
        M_Et( list( p(ceil(2*d_):end) ) , col ) = 0;
    end
end

d = sum(E');
d_= mean(full(d));
for row=1:n
    if ( sum(E(row,:))>2*d_ )
        list = find( E(row,:) > 0 );
        p = randperm(length(list));
        M_Et(row,list( p(ceil(2*d_):end) ) ) = 0;
    end
end

%% STEP 2: Sparse SVD
fprintf(1,'Sparse SVD\n');
[U0 S0 V0] = svds(M_Et, r); % M_Et = U0*S0*V0'

clear M_Et;

%% STEP 3: Initial Guess
fprintf(1,'Initial Guess\n');

S0 = S0*(m*n/nnz(E));
S0 = sqrt(S0);
U0 = (U0*S0)';
V0 = S0*V0';
X0 = [U0(:); V0(:)];

%% STEP 4: Optimization
fprintf(1,'Optimization\n');

options = struct; options.Method = 'lbfgs'; options.maxIter = 400;
X1 = minFunc(@(X) LogisticLoss(X,S,F), X0, options);

%% STEP 5: Return
fprintf(1,'Return\n');

if rescal_param ~= Inf
    X1 = X1/rescal_param;
end
X1 = reshape(X1, r, n+m);
U = X1(:,1:n); V = X1(:,(n+1):(n+m));

end
