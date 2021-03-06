package net.wendal.nutzbook.quartz.job;

import java.io.IOException;

import net.wendal.nutzbook.service.TopicService;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@IocBean
public class FaqIndexCommitJob implements Job {
	
	private static final Log log = Logs.get();

	@Inject TopicService topicService;
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			topicService.commitIndex();
		} catch (IOException e) {
			log.debug("commit faq index fail!!", e);
		}
	}

}
