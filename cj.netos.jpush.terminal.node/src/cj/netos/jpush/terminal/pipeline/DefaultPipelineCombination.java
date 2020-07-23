package cj.netos.jpush.terminal.pipeline;


import cj.netos.jpush.CombineException;
import cj.netos.jpush.IPipeline;
import cj.netos.jpush.IPipelineCombination;
import cj.netos.jpush.terminal.IEndPortContainer;
import cj.netos.jpush.terminal.pipeline.valve.CheckSecurityValve;
import cj.netos.jpush.terminal.pipeline.valve.DispatchCommandValve;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.CircuitException;

public class DefaultPipelineCombination implements IPipelineCombination {

    @Override
    public void combine(IPipeline pipeline) throws CombineException {
        if (pipeline.isEmpty()) {
            pipeline.append(new CheckSecurityValve(pipeline.site()));
        }
        pipeline.append(new DispatchCommandValve(pipeline.site()));
    }

    @Override
    public void demolish(IPipeline pipeline) {
        IEndPortContainer container = (IEndPortContainer) pipeline.site().getService("$.terminal.endPortContainer");
        try {
            container.offline(pipeline.endPort());
        } catch (CircuitException e) {
            CJSystem.logging().error(getClass(), e);
        }
    }
}
