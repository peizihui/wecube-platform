package com.webank.wecube.platform.core.service.workflow;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.AuthenticationContextHolder;
import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.ProcDefInfoExportImportDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefParamDto;
import com.webank.wecube.platform.core.entity.workflow.ProcDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcRoleBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeParamEntity;
import com.webank.wecube.platform.core.jpa.workflow.ProcDefInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeDefInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeParamRepository;
import com.webank.wecube.platform.core.service.user.UserManagementServiceImpl;
import com.webank.wecube.platform.workflow.commons.LocalIdGenerator;

@Service
public class WorkflowProcDefMigrationService extends AbstractWorkflowProcDefService {
    private static final Logger log = LoggerFactory.getLogger(WorkflowProcDefMigrationService.class);
    
    @Autowired
    private ProcDefInfoRepository processDefInfoRepo;

    @Autowired
    private TaskNodeDefInfoRepository taskNodeDefInfoRepo;
    
    @Autowired
    private UserManagementServiceImpl userManagementService;
    
    @Autowired
    private TaskNodeParamRepository taskNodeParamRepo;
    
    public ProcDefInfoExportImportDto importProcessDefinition(ProcDefInfoExportImportDto importDto) {
        if (importDto == null) {
            throw new WecubeCoreException("3131","Invalid import data.");
        }

        Date currTime = new Date();

        ProcDefInfoExportImportDto result = new ProcDefInfoExportImportDto();

        ProcDefInfoEntity draftEntity = new ProcDefInfoEntity();
        draftEntity.setId(LocalIdGenerator.generateId());
        draftEntity.setStatus(ProcDefInfoEntity.DRAFT_STATUS);
        draftEntity.setCreatedBy(AuthenticationContextHolder.getCurrentUsername());

        draftEntity.setProcDefData(importDto.getProcDefData());
        draftEntity.setProcDefKey(importDto.getProcDefKey());
        draftEntity.setProcDefName(importDto.getProcDefName());
        draftEntity.setRootEntity(importDto.getRootEntity());
        draftEntity.setUpdatedTime(currTime);

        ProcDefInfoEntity savedProcDefInfoDraftEntity = processDefInfoRepo.save(draftEntity);
        log.info("process definition saved with id:{}", savedProcDefInfoDraftEntity.getId());
        String currentUsername = AuthenticationContextHolder.getCurrentUsername();
        List<String> roleIds = userManagementService.getRoleIdsByUsername(currentUsername);
        Map<String, List<String>> roleBinds = new HashMap<>();
        roleBinds.put(ProcRoleBindingEntity.permissionEnum.MGMT.name(), roleIds);

        ProcDefInfoDto tmpProcDefInfoDto = new ProcDefInfoDto();
        tmpProcDefInfoDto.setPermissionToRole(roleBinds);

        this.saveProcRoleBinding(savedProcDefInfoDraftEntity.getId(), tmpProcDefInfoDto);

        result.setProcDefData(draftEntity.getProcDefData());
        result.setProcDefKey(draftEntity.getProcDefKey());
        result.setProcDefName(draftEntity.getProcDefName());
        result.setRootEntity(draftEntity.getRootEntity());
        result.setStatus(draftEntity.getStatus());
        result.setProcDefId(draftEntity.getId());

        if (importDto.getTaskNodeInfos() != null) {
            for (TaskNodeDefInfoDto nodeDto : importDto.getTaskNodeInfos()) {
                TaskNodeDefInfoEntity draftNodeEntity = new TaskNodeDefInfoEntity();
                draftNodeEntity.setId(LocalIdGenerator.generateId());
                draftNodeEntity.setStatus(TaskNodeDefInfoEntity.DRAFT_STATUS);

                draftNodeEntity.setDescription(nodeDto.getDescription());
                draftNodeEntity.setNodeId(nodeDto.getNodeId());
                draftNodeEntity.setNodeName(nodeDto.getNodeName());
                draftNodeEntity.setProcDefId(draftEntity.getId());
                draftNodeEntity.setProcDefKey(draftEntity.getProcDefKey());
                draftNodeEntity.setRoutineExpression(nodeDto.getRoutineExpression());
                draftNodeEntity.setRoutineRaw(nodeDto.getRoutineRaw());
                draftNodeEntity.setServiceId(nodeDto.getServiceId());
                draftNodeEntity.setServiceName(nodeDto.getServiceName());
                draftNodeEntity.setTimeoutExpression(nodeDto.getTimeoutExpression());
                draftNodeEntity.setUpdatedTime(currTime);
                draftNodeEntity.setTaskCategory(nodeDto.getTaskCategory());

                taskNodeDefInfoRepo.save(draftNodeEntity);

                if (nodeDto.getParamInfos() != null && !nodeDto.getParamInfos().isEmpty()) {
                    for (TaskNodeDefParamDto nodeParamDto : nodeDto.getParamInfos()) {
                        TaskNodeParamEntity draftNodeParamEntity = new TaskNodeParamEntity();
                        draftNodeParamEntity.setId(LocalIdGenerator.generateId());
                        draftNodeParamEntity.setStatus(TaskNodeParamEntity.DRAFT_STATUS);

                        draftNodeParamEntity.setNodeId(StringUtils.isBlank(nodeParamDto.getNodeId())
                                ? nodeDto.getNodeId() : nodeParamDto.getNodeId());
                        draftNodeParamEntity.setBindNodeId(nodeParamDto.getBindNodeId());
                        draftNodeParamEntity.setBindParamName(nodeParamDto.getBindParamName());
                        draftNodeParamEntity.setBindParamType(nodeParamDto.getBindParamType());
                        draftNodeParamEntity.setParamName(nodeParamDto.getParamName());
                        draftNodeParamEntity.setProcDefId(draftEntity.getId());
                        draftNodeParamEntity.setTaskNodeDefId(draftNodeEntity.getId());
                        draftNodeParamEntity.setUpdatedTime(currTime);
                        draftNodeParamEntity.setBindType(nodeParamDto.getBindType());
                        draftNodeParamEntity.setBindValue(nodeParamDto.getBindValue());

                        taskNodeParamRepo.save(draftNodeParamEntity);

                    }
                }

                TaskNodeDefInfoDto nodeDtoResult = new TaskNodeDefInfoDto();
                nodeDtoResult.setNodeDefId(draftNodeEntity.getId());
                nodeDtoResult.setNodeId(draftNodeEntity.getNodeId());
                nodeDtoResult.setNodeName(draftNodeEntity.getNodeName());
                nodeDtoResult.setStatus(draftNodeEntity.getStatus());

                result.addTaskNodeInfos(nodeDtoResult);

            }
        }

        return result;

    }

    public ProcDefInfoExportImportDto exportProcessDefinition(String procDefId) {
        if (StringUtils.isBlank(procDefId)) {
            throw new WecubeCoreException("3132","Process definition id is blank.");
        }

        Optional<ProcDefInfoEntity> procDefOpt = processDefInfoRepo.findById(procDefId);

        if (!procDefOpt.isPresent()) {
            log.error("such process definition does not exist:{}", procDefId);
            throw new WecubeCoreException("3133","Such process defintion does not exist.");
        }

        ProcDefInfoEntity procDef = procDefOpt.get();

        if (!ProcDefInfoEntity.DEPLOYED_STATUS.equalsIgnoreCase(procDef.getStatus())) {
            log.error("unexpected process definition status,expected {} but {} for {}",
                    ProcDefInfoEntity.DEPLOYED_STATUS, procDef.getStatus(), procDef.getId());

            throw new WecubeCoreException("3134","Unexpected process status.Only deployed status meets.");
        }

        ProcDefInfoExportImportDto result = new ProcDefInfoExportImportDto();
        result.setProcDefId(procDef.getId());
        result.setRootEntity(procDef.getRootEntity());
        result.setStatus(procDef.getStatus());
        result.setCreatedTime(formatDate(procDef.getCreatedTime()));
        result.setProcDefData(procDef.getProcDefData());
        result.setProcDefKey(procDef.getProcDefKey());
        result.setProcDefName(procDef.getProcDefName());
        result.setProcDefVersion(String.valueOf(procDef.getProcDefVersion()));

        List<TaskNodeDefInfoEntity> taskNodeDefEntities = taskNodeDefInfoRepo.findAllByProcDefId(procDef.getId());
        for (TaskNodeDefInfoEntity nodeEntity : taskNodeDefEntities) {
            TaskNodeDefInfoDto tdto = taskNodeDefInfoDtoFromEntity(nodeEntity);

            List<TaskNodeParamEntity> taskNodeParamEntities = taskNodeParamRepo
                    .findAllByProcDefIdAndTaskNodeDefId(procDef.getId(), nodeEntity.getId());

            for (TaskNodeParamEntity tnpe : taskNodeParamEntities) {
                TaskNodeDefParamDto pdto = taskNodeDefParamDtoFromEntity(tnpe);

                tdto.addParamInfos(pdto);
            }

            result.addTaskNodeInfos(tdto);
        }

        return result;
    }

}
