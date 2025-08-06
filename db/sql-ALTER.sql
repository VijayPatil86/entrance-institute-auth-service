alter table USER_LOGIN add column ROLE varchar(50) not null default 'APPLICANT';
alter table USER_LOGIN add constraint check_USER_ROLE check(ROLE in ('APPLICANT'));
