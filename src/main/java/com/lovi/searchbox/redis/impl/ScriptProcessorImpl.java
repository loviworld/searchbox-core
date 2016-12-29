package com.lovi.searchbox.redis.impl;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.lovi.searchbox.config.SearchBoxConfig;
import com.lovi.searchbox.redis.JedisConnectionPool;
import com.lovi.searchbox.redis.ScriptProcessor;
import static com.lovi.searchbox.config.ConfigKeys.scripts_container_perfix;

public class ScriptProcessorImpl implements ScriptProcessor {

	private JedisPool pool = null;
    
	private SearchBoxConfig config;
	
	public ScriptProcessorImpl(SearchBoxConfig config, JedisConnectionPool jedisConnectionPool){
		this.pool = jedisConnectionPool.getConnection();
		this.config = config;
	}
	
	@Override
	public Object runScript(String script, List<String> keys, List<String> argv) throws Exception{
		try(Jedis jedis = pool.getResource()){
			return jedis.eval(script, keys, argv);
		}
	}
	
	@Override
	public Object runScriptByEvalsha(String name, List<String> keys, List<String> argv) throws Exception{
		try(Jedis jedis = pool.getResource()){
			String hashScript = jedis.hmget(createKey(scripts_container_perfix), name).get(0);
			return jedis.evalsha(hashScript, keys, argv);
		}
	}
	
	@Override
	public String saveScript(String name, String script) throws Exception{
		try(Jedis jedis = pool.getResource()){
			String hashScript = jedis.scriptLoad(script);
			Map<String, String> scriptsMap = new HashMap<>();
			scriptsMap.put(name, hashScript);
			jedis.hmset(createKey(scripts_container_perfix), scriptsMap);
			return hashScript;
		}
	}
	
	private String createKey(String prefix) {
		return config.getAppname() + ":" + prefix;
	}
}
