package com.wizzardo.tools.collections.flow.flows;

import com.wizzardo.tools.collections.flow.Flow;
import com.wizzardo.tools.collections.flow.Mapper;

/**
 * Created by wizzardo on 16.04.16.
 */
public class FlowMapMerge<A, B> extends Flow<A, B> {
    final Flow<B, B> proxy = new Flow<B, B>() {
        @Override
        public void process(B b) {
            FlowMapMerge.this.child.process(b);
        }

        @Override
        protected void onEnd() {
        }
    };
    final Mapper<? super A, ? extends Flow<? extends B, ? extends B>> mapper;

    public FlowMapMerge(Mapper<? super A, ? extends Flow<? extends B, ? extends B>> mapper) {
        this.mapper = mapper;
    }


    @Override
    public void process(A a) {
        Flow<? extends B, ? extends B> l = mapper.map(a);
        setChildTo(l, proxy);
        start(l);
    }
}