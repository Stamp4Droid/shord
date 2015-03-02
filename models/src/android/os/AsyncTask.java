import edu.stanford.stamp.annotation.Inline;

class AsyncTask
{
	@Inline
	public final  android.os.AsyncTask<Params, Progress, Result> execute(Params... params)
	{
		onPreExecute();
		Result result = doInBackground(params);
		onPostExecute(result);
		return this;
	}
}
