public class Classifier {
    private static double dot(double[] v1, double[] v2) {
	double dotProduct = 0.0;
	for(int i=0; i<v1.length; i++) {
	    dotProduct += v1[i]*v2[i];
	}
	return dotProduct;
    }

    private static double sigmoid(double x) {
	return 1.0/(1.0 + Math.exp(-x));
    }

    // assume x[0] = 1
    public static double predict(double[] theta, double[] x) {
	return sigmoid(dot(x, theta));
    }

    private static double logLikelihood(double[] theta, double[][] x, double[] y) {
	double likelihood = 0.0;
	for(int i=0; i<x.length; i++) {
	    double p = predict(theta, x[i]);
	    likelihood += y[i]*Math.log(p) + (1.0-y[i])*Math.log(1-p);
	}
	return likelihood;
    }

    private static double[] likelihoodDerivative(double[] theta, double[][] x, double[] y) {
	double[] derivative = new double[theta.length];
	for(int i=0; i<x.length; i++) {
	    for(int j=0; j<theta.length; j++) {
		double p = predict(theta, x[i]);
		derivative[j] += (y[i]-p)*x[i][j];
	    }
	}
	return derivative;
    }

    public static double[] maximumLikelihood(double[][] x, double[] y, double alpha) {
	double[] theta = new double[x[0].length];
	for(int i=0; i<100; i++) {
	    double[] thetaDerivative = likelihoodDerivative(theta, x, y);
	    for(int j=0; i<theta.length; i++) {
		theta[j] += alpha*thetaDerivative[j];
	    }
	}
	return theta;
    }

    public static void main(String[] args) {
	
    }
}

