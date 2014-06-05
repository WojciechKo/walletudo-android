package info.korzeniowski.walletplus.datamanager.local;

import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.datamanager.AccountDataManager;
import info.korzeniowski.walletplus.datamanager.local.modelfactory.AccountFactory;
import info.korzeniowski.walletplus.model.Account;
import info.korzeniowski.walletplus.model.greendao.GreenAccountDao;

public class LocalAccountDataManager implements AccountDataManager{
    private final GreenAccountDao greenAccountDao;
    private final List<Account> accounts;

    @Inject
    public LocalAccountDataManager(GreenAccountDao greenAccountDao) {
        this.greenAccountDao = greenAccountDao;
        accounts = AccountFactory.createAccountList(greenAccountDao.loadAll());
    }

    @Override
    public Long insert(Account entity) {
        return null;
    }

    @Override
    public Long count() {
        return null;
    }

    @Override
    public Account findById(Long id) {
        return null;
    }

    @Override
    public List<Account> getAll() {
        return null;
    }

    @Override
    public void update(Account entity) {

    }

    @Override
    public void deleteById(Long id) {

    }
}