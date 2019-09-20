package org.meveo.admin.action.storage;

import org.apache.commons.collections.CollectionUtils;
import org.meveo.admin.action.BaseCrudBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.git.GitRepositoryDto;
import org.meveo.api.git.GitRepositoryApi;
import org.meveo.elresolver.ELException;
import org.meveo.model.crm.custom.ReadingRoleTypeEnum;
import org.meveo.model.crm.custom.WritingRoleTypeEnum;
import org.meveo.model.git.GitRepository;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.git.GitRepositoryService;
import org.primefaces.model.DualListModel;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class GitRepositoryBean extends BaseCrudBean<GitRepository, GitRepositoryDto> {

    private static final long serialVersionUID = 8661265102557481231L;

    @Inject
    private GitRepositoryService gitRepositoryService;

    @Inject
    private GitRepositoryApi gitRepositoryApi;

    private DualListModel<ReadingRoleTypeEnum> readingRolesDM;
    private DualListModel<WritingRoleTypeEnum> writingRolesDM;

    public GitRepositoryBean() {
        super(GitRepository.class);
    }

    @Override
    public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {

        if (CollectionUtils.isNotEmpty(readingRolesDM.getTarget())) {
            getEntity().getReadingRoles().clear();
            for (ReadingRoleTypeEnum readingRoleTypeEnum : readingRolesDM.getTarget()) {
                getEntity().getReadingRoles().add(readingRoleTypeEnum.name());
            }
        } else {
            getEntity().getReadingRoles().clear();
        }

        if (CollectionUtils.isNotEmpty(writingRolesDM.getTarget())) {
            getEntity().getWritingRoles().clear();
            for (WritingRoleTypeEnum writingRoleTypeEnum : writingRolesDM.getTarget()) {
                getEntity().getWritingRoles().add(writingRoleTypeEnum.name());
            }
        } else {
            getEntity().getWritingRoles().clear();
        }
        return super.saveOrUpdate(killConversation);
    }

    public DualListModel<ReadingRoleTypeEnum> getReadingRolesDM() {
        if (readingRolesDM == null) {
            List<ReadingRoleTypeEnum> perksSource = new ArrayList<>();
            for (ReadingRoleTypeEnum readingRoleTypeEnum : ReadingRoleTypeEnum.values()) {
                perksSource.add(readingRoleTypeEnum);
            }
            List<ReadingRoleTypeEnum> perksTarget = new ArrayList<>();
            if (getEntity().getReadingRoles() != null) {
                for (String action : getEntity().getReadingRoles()) {
                    perksTarget.add(ReadingRoleTypeEnum.valueOf(action));
                }
            }
            perksSource.removeAll(perksTarget);
            readingRolesDM = new DualListModel<ReadingRoleTypeEnum>(perksSource, perksTarget);
        }
        return readingRolesDM;
    }

    public List<ReadingRoleTypeEnum> getReadingRolesTypeList() {
        ArrayList<ReadingRoleTypeEnum> arrayList = new ArrayList<>(readingRolesDM.getSource());
        arrayList.addAll(readingRolesDM.getTarget());
        return arrayList;
    }

    public void setReadingRolesDM(DualListModel<ReadingRoleTypeEnum> readingRolesDM) {
        this.readingRolesDM = readingRolesDM;
    }

    public DualListModel<WritingRoleTypeEnum> getWritingRolesDM() {
        if (writingRolesDM == null) {
            List<WritingRoleTypeEnum> perksSource = new ArrayList<>();
            for (WritingRoleTypeEnum writingRoleTypeEnum : WritingRoleTypeEnum.values()) {
                perksSource.add(writingRoleTypeEnum);
            }
            List<WritingRoleTypeEnum> perksTarget = new ArrayList<>();
            if (getEntity().getWritingRoles() != null) {
                for (String action : getEntity().getWritingRoles()) {
                    perksTarget.add(WritingRoleTypeEnum.valueOf(action));
                }
            }
            perksSource.removeAll(perksTarget);
            writingRolesDM = new DualListModel<WritingRoleTypeEnum>(perksSource, perksTarget);
        }
        return writingRolesDM;
    }

    public List<WritingRoleTypeEnum> getWritingRolesTypeList() {
        ArrayList<WritingRoleTypeEnum> arrayList = new ArrayList<>(writingRolesDM.getSource());
        arrayList.addAll(writingRolesDM.getTarget());
        return arrayList;
    }

    public void setWritingRolesDM(DualListModel<WritingRoleTypeEnum> writingRolesDM) {
        this.writingRolesDM = writingRolesDM;
    }

    @Override
    protected String getListViewName() {
        return "gitRepositories";
    }

    @Override
    public BaseCrudApi<GitRepository, GitRepositoryDto> getBaseCrudApi() {
        return gitRepositoryApi;
    }

    @Override
    protected IPersistenceService<GitRepository> getPersistenceService() {
        return gitRepositoryService;
    }


}
