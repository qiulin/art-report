-- Add foreign keys to ART database tables

-- the unique constraints below may be needed for databases created before 3.3
-- ALTER TABLE ART_USERS ADD CONSTRAINT au_uq_u_id UNIQUE(USER_ID);
-- ALTER TABLE ART_RULES ADD CONSTRAINT ar_uq_r_id UNIQUE(RULE_ID);
-- ALTER TABLE ART_JOB_SCHEDULES ADD CONSTRAINT ajs_uq_s_id UNIQUE(SCHEDULE_ID);

ALTER TABLE ART_USERS ADD CONSTRAINT au_fk_acc_lvl FOREIGN KEY(ACCESS_LEVEL) REFERENCES ART_ACCESS_LEVELS(ACCESS_LEVEL);
ALTER TABLE ART_USERS ADD CONSTRAINT au_fk_def_qr_grp FOREIGN KEY(DEFAULT_QUERY_GROUP) REFERENCES ART_QUERY_GROUPS(QUERY_GROUP_ID);
ALTER TABLE ART_USER_GROUPS ADD CONSTRAINT aug_fk_dqg FOREIGN KEY(DEFAULT_QUERY_GROUP) REFERENCES ART_QUERY_GROUPS(QUERY_GROUP_ID);
ALTER TABLE ART_USER_GROUP_ASSIGNMENT ADD CONSTRAINT auga_fk_ug_id FOREIGN KEY(USER_GROUP_ID) REFERENCES ART_USER_GROUPS(USER_GROUP_ID);
ALTER TABLE ART_USER_GROUP_ASSIGNMENT ADD CONSTRAINT auga_fk_u_id FOREIGN KEY(USER_ID) REFERENCES ART_USERS(USER_ID);
ALTER TABLE ART_QUERIES ADD CONSTRAINT aq_fk_qt FOREIGN KEY(QUERY_TYPE) REFERENCES ART_REPORT_TYPES(REPORT_TYPE);
ALTER TABLE ART_QUERIES ADD CONSTRAINT aq_fk_ds_id FOREIGN KEY(DATASOURCE_ID) REFERENCES ART_DATABASES(DATABASE_ID);
ALTER TABLE ART_QUERIES ADD CONSTRAINT aq_fk_enc_id FOREIGN KEY(ENCRYPTOR_ID) REFERENCES ART_ENCRYPTORS(ENCRYPTOR_ID);
ALTER TABLE ART_REPORT_REPORT_GROUPS ADD CONSTRAINT arrg_fk_r_id FOREIGN KEY(REPORT_ID) REFERENCES ART_QUERIES(QUERY_ID);
ALTER TABLE ART_REPORT_REPORT_GROUPS ADD CONSTRAINT arrg_fk_rg_id FOREIGN KEY(REPORT_GROUP_ID) REFERENCES ART_QUERY_GROUPS(QUERY_GROUP_ID);
ALTER TABLE ART_ADMIN_PRIVILEGES ADD CONSTRAINT aap_fk_u_id FOREIGN KEY(USER_ID) REFERENCES ART_USERS(USER_ID);
ALTER TABLE ART_USER_QUERIES ADD CONSTRAINT auq_fk_u_id FOREIGN KEY(USER_ID) REFERENCES ART_USERS(USER_ID);
ALTER TABLE ART_USER_QUERIES ADD CONSTRAINT auq_fk_q_id FOREIGN KEY(QUERY_ID) REFERENCES ART_QUERIES(QUERY_ID);
ALTER TABLE ART_USER_QUERY_GROUPS ADD CONSTRAINT auqg_fk_u_id FOREIGN KEY(USER_ID) REFERENCES ART_USERS(USER_ID);
ALTER TABLE ART_USER_QUERY_GROUPS ADD CONSTRAINT auqg_fk_qg_id FOREIGN KEY(QUERY_GROUP_ID) REFERENCES ART_QUERY_GROUPS(QUERY_GROUP_ID);
ALTER TABLE ART_PARAMETERS ADD CONSTRAINT ap_fk_dv_rid FOREIGN KEY(DEFAULT_VALUE_REPORT_ID) REFERENCES ART_QUERIES(QUERY_ID);
ALTER TABLE ART_PARAMETERS ADD CONSTRAINT ap_fk_lov_rid FOREIGN KEY(LOV_REPORT_ID) REFERENCES ART_QUERIES(QUERY_ID);
ALTER TABLE ART_REPORT_PARAMETERS ADD CONSTRAINT arp_fk_rid FOREIGN KEY(REPORT_ID) REFERENCES ART_QUERIES(QUERY_ID);
ALTER TABLE ART_REPORT_PARAMETERS ADD CONSTRAINT arp_fk_p_id FOREIGN KEY(PARAMETER_ID) REFERENCES ART_PARAMETERS(PARAMETER_ID);
ALTER TABLE ART_QUERY_RULES ADD CONSTRAINT aqr_fk_q_id FOREIGN KEY(QUERY_ID) REFERENCES ART_QUERIES(QUERY_ID);
ALTER TABLE ART_QUERY_RULES ADD CONSTRAINT aqr_fk_r_id FOREIGN KEY(RULE_ID) REFERENCES ART_RULES(RULE_ID);
ALTER TABLE ART_USER_RULES ADD CONSTRAINT aur_fk_u_id FOREIGN KEY(USER_ID) REFERENCES ART_USERS(USER_ID);
ALTER TABLE ART_USER_RULES ADD CONSTRAINT aur_fk_r_id FOREIGN KEY(RULE_ID) REFERENCES ART_RULES(RULE_ID);
ALTER TABLE ART_USER_GROUP_RULES ADD CONSTRAINT augr_fk_ug_id FOREIGN KEY(USER_GROUP_ID) REFERENCES ART_USER_GROUPS(USER_GROUP_ID);
ALTER TABLE ART_USER_GROUP_RULES ADD CONSTRAINT augr_fk_r_id FOREIGN KEY(RULE_ID) REFERENCES ART_RULES(RULE_ID);
ALTER TABLE ART_JOBS ADD CONSTRAINT aj_fk_q_id FOREIGN KEY(QUERY_ID) REFERENCES ART_QUERIES(QUERY_ID);
ALTER TABLE ART_JOBS ADD CONSTRAINT aj_fk_u_id FOREIGN KEY(USER_ID) REFERENCES ART_USERS(USER_ID);
ALTER TABLE ART_JOBS ADD CONSTRAINT aj_fk_r_q_id FOREIGN KEY(RECIPIENTS_QUERY_ID) REFERENCES ART_QUERIES(QUERY_ID);
ALTER TABLE ART_JOBS ADD CONSTRAINT aj_fk_s_id FOREIGN KEY(SCHEDULE_ID) REFERENCES ART_JOB_SCHEDULES(SCHEDULE_ID);
ALTER TABLE ART_JOBS ADD CONSTRAINT aj_fk_ss_id FOREIGN KEY(SMTP_SERVER_ID) REFERENCES ART_SMTP_SERVERS(SMTP_SERVER_ID);
ALTER TABLE ART_JOBS_PARAMETERS ADD CONSTRAINT ajp_fk_j_id FOREIGN KEY(JOB_ID) REFERENCES ART_JOBS(JOB_ID);
ALTER TABLE ART_USER_JOBS ADD CONSTRAINT auj_fk_j_id FOREIGN KEY(JOB_ID) REFERENCES ART_JOBS(JOB_ID);
ALTER TABLE ART_USER_JOBS ADD CONSTRAINT auj_fk_u_id FOREIGN KEY(USER_ID) REFERENCES ART_USERS(USER_ID);
ALTER TABLE ART_USER_JOBS ADD CONSTRAINT auj_fk_ug_id FOREIGN KEY(USER_GROUP_ID) REFERENCES ART_USER_GROUPS(USER_GROUP_ID);
ALTER TABLE ART_USER_GROUP_QUERIES ADD CONSTRAINT augq_fk_ug_id FOREIGN KEY(USER_GROUP_ID) REFERENCES ART_USER_GROUPS(USER_GROUP_ID);
ALTER TABLE ART_USER_GROUP_QUERIES ADD CONSTRAINT augq_fk_q_id FOREIGN KEY(QUERY_ID) REFERENCES ART_QUERIES(QUERY_ID);
ALTER TABLE ART_USER_GROUP_GROUPS ADD CONSTRAINT augg_fk_ug_id FOREIGN KEY(USER_GROUP_ID) REFERENCES ART_USER_GROUPS(USER_GROUP_ID);
ALTER TABLE ART_USER_GROUP_GROUPS ADD CONSTRAINT augg_fk_qg_id FOREIGN KEY(QUERY_GROUP_ID) REFERENCES ART_QUERY_GROUPS(QUERY_GROUP_ID);
ALTER TABLE ART_USER_GROUP_JOBS ADD CONSTRAINT augj_fk_ug_id FOREIGN KEY(USER_GROUP_ID) REFERENCES ART_USER_GROUPS(USER_GROUP_ID);
ALTER TABLE ART_USER_GROUP_JOBS ADD CONSTRAINT augj_fk_j_id FOREIGN KEY(JOB_ID) REFERENCES ART_JOBS(JOB_ID);
ALTER TABLE ART_DRILLDOWN_QUERIES ADD CONSTRAINT adq_fk_q_id FOREIGN KEY(QUERY_ID) REFERENCES ART_QUERIES(QUERY_ID);
ALTER TABLE ART_DRILLDOWN_QUERIES ADD CONSTRAINT adq_fk_dq_id FOREIGN KEY(DRILLDOWN_QUERY_ID) REFERENCES ART_QUERIES(QUERY_ID);
ALTER TABLE ART_JOB_ARCHIVES ADD CONSTRAINT aja_fk_j_id FOREIGN KEY(JOB_ID) REFERENCES ART_JOBS(JOB_ID);
ALTER TABLE ART_JOB_ARCHIVES ADD CONSTRAINT aja_fk_u_id FOREIGN KEY(USER_ID) REFERENCES ART_USERS(USER_ID);
ALTER TABLE ART_LOGGED_IN_USERS ADD CONSTRAINT alu_fk_u_id FOREIGN KEY(USER_ID) REFERENCES ART_USERS(USER_ID);
ALTER TABLE ART_SCHEDULE_HOLIDAY_MAP ADD CONSTRAINT ashm_fk_s_id FOREIGN KEY(SCHEDULE_ID) REFERENCES ART_JOB_SCHEDULES(SCHEDULE_ID);
ALTER TABLE ART_SCHEDULE_HOLIDAY_MAP ADD CONSTRAINT ashm_fk_h_id FOREIGN KEY(HOLIDAY_ID) REFERENCES ART_HOLIDAYS(HOLIDAY_ID);
ALTER TABLE ART_JOB_HOLIDAY_MAP ADD CONSTRAINT ajhm_fk_j_id FOREIGN KEY(JOB_ID) REFERENCES ART_JOBS(JOB_ID);
ALTER TABLE ART_JOB_HOLIDAY_MAP ADD CONSTRAINT ajhm_fk_h_id FOREIGN KEY(HOLIDAY_ID) REFERENCES ART_HOLIDAYS(HOLIDAY_ID);
ALTER TABLE ART_JOB_DESTINATION_MAP ADD CONSTRAINT ajdm_fk_j_id FOREIGN KEY(JOB_ID) REFERENCES ART_JOBS(JOB_ID);
ALTER TABLE ART_JOB_DESTINATION_MAP ADD CONSTRAINT ajdm_fk_d_id FOREIGN KEY(DESTINATION_ID) REFERENCES ART_DESTINATIONS(DESTINATION_ID);
ALTER TABLE ART_SAVED_PARAMETERS ADD CONSTRAINT asp_fk_u_id FOREIGN KEY(USER_ID) REFERENCES ART_USERS(USER_ID);
ALTER TABLE ART_SAVED_PARAMETERS ADD CONSTRAINT asp_fk_r_id FOREIGN KEY(REPORT_ID) REFERENCES ART_QUERIES(QUERY_ID);
ALTER TABLE ART_USER_PARAM_DEFAULTS ADD CONSTRAINT aupd_fk_u_id FOREIGN KEY(USER_ID) REFERENCES ART_USERS(USER_ID);
ALTER TABLE ART_USER_PARAM_DEFAULTS ADD CONSTRAINT aupd_fk_p_id FOREIGN KEY(PARAMETER_ID) REFERENCES ART_PARAMETERS(PARAMETER_ID);
ALTER TABLE ART_USER_GROUP_PARAM_DEFAULTS ADD CONSTRAINT augpd_fk_ug_id FOREIGN KEY(USER_GROUP_ID) REFERENCES ART_USER_GROUPS(USER_GROUP_ID);
ALTER TABLE ART_USER_GROUP_PARAM_DEFAULTS ADD CONSTRAINT augpd_fk_p_id FOREIGN KEY(PARAMETER_ID) REFERENCES ART_PARAMETERS(PARAMETER_ID);
ALTER TABLE ART_USER_FIXED_PARAM_VAL ADD CONSTRAINT aufpv_fk_u_id FOREIGN KEY(USER_ID) REFERENCES ART_USERS(USER_ID);
ALTER TABLE ART_USER_FIXED_PARAM_VAL ADD CONSTRAINT aufpv_fk_p_id FOREIGN KEY(PARAMETER_ID) REFERENCES ART_PARAMETERS(PARAMETER_ID);
ALTER TABLE ART_USER_GROUP_FIXED_PARAM_VAL ADD CONSTRAINT augfpv_fk_ug_id FOREIGN KEY(USER_GROUP_ID) REFERENCES ART_USER_GROUPS(USER_GROUP_ID);
ALTER TABLE ART_USER_GROUP_FIXED_PARAM_VAL ADD CONSTRAINT augfpv_fk_p_id FOREIGN KEY(PARAMETER_ID) REFERENCES ART_PARAMETERS(PARAMETER_ID);
