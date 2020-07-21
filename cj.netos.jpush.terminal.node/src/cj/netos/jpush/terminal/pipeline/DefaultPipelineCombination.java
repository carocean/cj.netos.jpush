package cj.netos.jpush.terminal.pipeline;


import cj.netos.jpush.terminal.CombineException;
import cj.netos.jpush.terminal.IPipeline;
import cj.netos.jpush.terminal.IPipelineCombination;

public class DefaultPipelineCombination implements IPipelineCombination {

    @Override
    public void combine(IPipeline pipeline) throws CombineException {
//        if (pipeline.isEmpty()) {
//            pipeline.append(new CheckSecurityValve(pipeline.site()));
//        }
//        pipeline.append(new DispatchCommandValve(pipeline.site()));
//        pipeline.append(new UpstreamEndportValve(pipeline.site()));
//        pipeline.append(new ErrorValve(pipeline.site()));

    }

    @Override
    public void demolish(IPipeline pipeline) {
    }
}
