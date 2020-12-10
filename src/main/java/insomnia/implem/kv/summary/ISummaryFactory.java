package insomnia.kv.summary;

import java.io.InputStream;
import java.io.OutputStream;

public interface ISummaryFactory
{
	public ISummary generate(Iterable<? extends Object> datas);

	public ISummary load(InputStream in);

	public void save(OutputStream out, ISummary summary);
}
