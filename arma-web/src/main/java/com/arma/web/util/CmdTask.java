package com.arma.web.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CmdTask
{
    private volatile boolean _stop;
    private Process _process;

    private int _log_level;
    private boolean _use_file;
    private String _cmd;
    private File _path;
    private List<String> list = new ArrayList<String>();
    private static final Log _logger = LogFactory.getLog("web.CmdTask");

    // log_level - 0 no log, 1 err, 2 err and std
    public CmdTask(int log_level, boolean use_file, String cmd, File path)
    {
        _log_level = log_level;
        _use_file = use_file;
        _cmd = cmd;
        _path = path;
    }

    public final void terminate()
    {
        _stop = true;
        if (_process != null)
            _process.destroy();
    }

    public List<String> execute()
    {
        list.clear();
        if (WebUtil.empty(_cmd))
            return list;

        CmdGobbler input_thread = null, error_thread = null;
        try
        {
            _process = WebUtil.exec(_use_file, _cmd, _path);
            if (_process == null)
                return list;

            // handle process error and output stream
            input_thread = new CmdGobbler(_log_level, false, _process.getInputStream());
            error_thread = new CmdGobbler(_log_level, true, _process.getErrorStream());
            input_thread.start();
            error_thread.start();

            // wait for everything to finish
            _process.waitFor();
            input_thread.join(500L);
            error_thread.join(500L);
            int exit = _process.exitValue();
            if (_log_level > 0)
                _logger.info("cmd return, code " + exit + (_use_file ? ", file " + _cmd : ", cmd " + _cmd));
        }
        catch (InterruptedException e)
        {
        }
        catch (Exception e)
        {
            if (_log_level > 0)
                _logger.error("cmd fail, " + e);
        }
        finally
        {
            if (input_thread != null)
            {
                list.addAll(input_thread.lines());
                input_thread.terminate();
            }
            if (error_thread != null)
            {
                list.addAll(error_thread.lines());
                error_thread.terminate();
            }
            _stop = true;
            if (_process != null)
                _process.destroy();
            _process = null;
        }
        return list;
    }

    public List<String> lines()
    {
        return list;
    }

    public boolean stopped()
    {
        return _stop;
    }
}
