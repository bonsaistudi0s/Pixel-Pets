package com.bonsai.pixelpets.util.describers;

import com.bonsai.pixelpets.pixelpets.registration.data.ability.conditions.TargetCondition;
import net.minecraft.network.chat.Component;

public class TargetConditionDescriber implements ChangingObjectDescriber<TargetCondition> {

    @Override
    public boolean sameIdentity(TargetCondition first, TargetCondition second) {
        return false;
    }

    @Override
    public Component describe(TargetCondition condition) {
        return condition.describe();
    }

    @Override
    public Component describeSimply(TargetCondition condition) {
        return condition.describe();
    }

    @Override
    public Component describeModification(ObjectModification<TargetCondition> modification) {
        // Should ignore this, handling exact changes is too much work for now
        return Component.empty();
    }
}
