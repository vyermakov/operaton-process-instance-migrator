package com.jeevision.operaton.migrator.plan;

import org.operaton.bpm.engine.ProcessEngine;
import org.operaton.bpm.engine.migration.MigrationPlan;

import com.jeevision.operaton.migrator.instances.VersionedProcessInstance;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreatePatchMigrationplanDefaultImpl implements CreatePatchMigrationplan {

	private final ProcessEngine processEngine;

	@Override
    public MigrationPlan migrationPlanByMappingEqualActivityIDs(VersionedDefinitionId newestProcessDefinition, VersionedProcessInstance processInstance) {
        return processEngine.getRuntimeService()
                .createMigrationPlan(processInstance.getProcessDefinitionId(), newestProcessDefinition.getProcessDefinitionId())
                .mapEqualActivities()
                .updateEventTriggers()
                .build();
    }
}
