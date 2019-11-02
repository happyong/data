package com.arma.web.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CmdGobbler extends Thread
{
    private volatile boolean _stop;
    private boolean _log;
    private String _prefix;
    private List<String> list = new ArrayList<String>();

    private InputStream _is;
    private static final Log _logger = LogFactory.getLog("web.CmdGobbler");

    // log_level - 0 no log, 1 err, 2 err and std
    public CmdGobbler(int log_level, boolean error, InputStream is)
    {
        _log = ((error && log_level > 0) || (!error && log_level == 2));
        _prefix = (error ? "[ERR] - " : "[STD] - ");
        _is = is;
    }

    @Override
    final public void run()
    {
        list.clear();
        BufferedReader br = null;
        try
        {
            br = new BufferedReader(new InputStreamReader(_is));
            String line = null;
            while (!_stop && (line = br.readLine()) != null)
            {
                if (line.length() == 0)
                    continue;
                list.add(line);
                if (_log)
                    _logger.info(_prefix + line);
            }
        }
        catch (Exception e)
        {
        }
        finally
        {
            if (br != null)
                try
                {
                    br.close();
                }
                catch (Exception e)
                {
                }
        }
    }

    public final void terminate()
    {
        _stop = true;
        interrupt();
    }

    public List<String> lines()
    {
        return list;
    }
}
