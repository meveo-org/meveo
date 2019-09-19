package org.meveo.admin.action.storage;

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
        String editView = super.saveOrUpdate(killConversation);
        return editView;
    }

    public DualListModel<ReadingRoleTypeEnum> getReadingRolesDM() {
        if (readingRolesDM == null) {
            List<ReadingRoleTypeEnum> perksSource = new ArrayList<>();
            for (ReadingRoleTypeEnum readingRoleTypeEnum : ReadingRoleTypeEnum.values()) {
                perksSource.add(readingRoleTypeEnum);
            }
            List<ReadingRoleTypeEnum> perksTarget = new ArrayList<>();
            if (getEntity().getReadingRoles() != null) {
                List<ReadingRoleTypeEnum> allReadingRoles = (List<ReadingRoleTypeEnum>)(List<?>)(new ArrayList<>(getEntity().getReadingRoles()));
                perksTarget.addAll(allReadingRoles);
            }
            perksSource.removeAll(perksTarget);
            readingRolesDM = new DualListModel<>(perksSource, perksTarget);
        }
        return readingRolesDM;
    }

    public void setReadingRolesDM(DualListModel<ReadingRoleTypeEnum> readingRolesDM) {
        this.readingRolesDM = readingRolesDM;
    }

    public DualListModel<WritingRoleTypeEnum> getWritingRolesDM() {
        if (writingRolesDM == null) {
            List<WritingRoleTypeEnum> perksSource = new ArrayList<>();
            for (WritingRoleTypeEnum writingRoleType : WritingRoleTypeEnum.values()) {
                perksSource.add(writingRoleType);
            }
            List<WritingRoleTypeEnum> perksTarget = new ArrayList<>();
            if (getEntity().getWritingRoles() != null) {
                List<WritingRoleTypeEnum> allWritingRoles = (List<WritingRoleTypeEnum>)(List<?>)(new ArrayList<>(getEntity().getWritingRoles()));
                perksTarget.addAll(allWritingRoles);
            }
            perksSource.removeAll(perksTarget);
            writingRolesDM = new DualListModel<>(perksSource, perksTarget);
        }
        return writingRolesDM;
    }

    public void setWritingRolesDM(DualListModel<WritingRoleTypeEnum> writingRolesDM) {
        this.writingRolesDM = writingRolesDM;
    }

    @Override
    public String getEditViewName() {
        return "gitRepositoryDetail";
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
