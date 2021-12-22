set timezone to 'Asia/Chongqing';
create schema af_oauth;

create table af_oauth.managed_key
(
    id             bigserial,
    algorithm      varchar     not null,
    identity_id    varchar(24) not null,
    key            text        not null,
    public_key     text        not null,
    activated_on   timestamptz not null,
    deactivated_on timestamptz not null,
    deleted        int         not null default 0,
    constraint managed_key_pk primary key (id)
);
create index managed_key_idx_key_algorithm on af_oauth.managed_key (algorithm);

create table af_oauth.client_detail
(
    id                            bigserial,
    client_id                     varchar(36) not null,
    client_secret                 varchar(48) not null,
    authorization_grant_types     varchar[]   not null,
    client_authentication_methods varchar[]   not null,
    scopes                        varchar[]   not null,
    redirect_uris                 varchar[]   not null,
    create_time                     timestamptz not null,
    update_time                    timestamptz not null,
    user_id                       bigint      null,
    data_push_url                 varchar(60) null,
    deleted                       int         not null default 0,
    constraint client_detail_pk primary key (id),
    constraint client_detail_key_client_id unique (client_id)
);

INSERT INTO af_oauth.client_detail (id, client_id, client_secret, authorization_grant_types, client_authentication_methods,
                                 scopes, redirect_uris, create_time, update_time, user_id, data_push_url)
VALUES (1, 'messaging-client', 'secret', '{basic}', '{client_credentials}', '{orange}', '', now(), to_timestamp(0),
        null, null);
INSERT INTO af_oauth.client_detail (id, client_id, client_secret, authorization_grant_types, client_authentication_methods,
                                 scopes, redirect_uris, create_time, update_time, user_id, data_push_url)
VALUES (2, 'ec-client', 'secret', '{basic}', '{client_credentials}', '{orange}', '', now(), to_timestamp(0), null,
        null);
alter sequence af_oauth.client_detail_id_seq restart 3;

INSERT INTO af_oauth.managed_key (id, algorithm, identity_id, key, public_key, activated_on, deactivated_on)
VALUES (1, 'RSA', '1', '-----BEGIN RSA PRIVATE KEY-----
MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC0pgaGN9e8EzTm
z0xY+z2ua3l08q3YnBMQWCxJE+m8I3BiH5JIEKNB6C9XF9X0if0SThbb3xW0mO8E
1RAX/Oo8tVpp3vJDFHg7ZgYmnChC1GqSlLFuj9TTRwIwqwExTMdG3xURunY836hq
dD0KdF/nqcsV9x/UQ1PaCfDSLEoeuW6qLtXOSg9FjuhuLA79OHjIuB54JbfGcAKW
XdJcf8P2if0X6FEJyqQ8SfQJ6biuJL/BxCBiwESlLZPlOZcOHW77i/xVz3HDzibL
S/TWOSjEBZDoo+haybKa46IfT/n2g2ccI+Sh3uN7CdzcjuVGt+ImZXtKA6r/AIST
RMC8wJz5AgMBAAECggEAIP9Oo8ehhX4wpJRrCrnhu3FwPxdw/+cKaGris/qKME58
4b+IlddMKubBdvQopcFq06Ql8sWeDl39EvHkPa16D9rEiCAsOmqLx7XMG9NcW1C3
osy8WEi3hFwtHzytPBcdM2neTF216UqlVcSjvbwiJIvUR8/bJYei0moMbiee5luD
K3kFRWHKRvE/+E7cx3fVo+DCgf+qxbL/T4uoH/K0TEeXe1DeaZP7O5fE3A0qXBUO
n8hZGPL9V9EWF1LJ2DkD9Q/YRH6+/Goox3X5YklQYcljFc7GKIhiElIG6RQjOTLx
5nvzpOW47sSBaitcJlBwf+LlYD/oWpOTz6K4xZd43QKBgQD9C0cCqpnFsxUUlN95
/BrWaLhI5IiKb6z8iN6uvMbe8L8WaME6wD6b7kIGn5LVxhs2aMg9zWHtqhmC4UFV
w5vzvRQDuNilTMZkHsgptcdJuMyDLIWSiezLTByszz0UFrecHG/W6xIPDr1u40F7
zERRy2en+zGGCHFaiWgPzw6dfwKBgQC2wkA8Y0lN8MBLA/y6gbDTNAB3s5oqrekh
q6kwa6y+H6ZAlrk3d5+PnezjFxu/fM3LBj7zeKWGM/mHli6BVsMsHij9PxVj7nvg
fiLQ1LoYdd/3Z5uheDBmK4LdCd2kjqLnFu49TIe3kG4K/VPdDMbG5tHUsd7FIGPJ
gIQgh7nxhwKBgQDQ71vdvsGjdN/GE7qGfXwnZ2YqgdCgBd/e0KCVxTer4zrUpQBP
o2bO16ba0y2pp57WiSQ1q3zdgWL5J0cMKqx9T7TT8e+oZUE2cBg0IG0B2T28XamY
upuzrQ/MPH2hNbS7iNtrqMNLfY86nRh3wMz0gLABCq70jcoSyHwM+ZmGvwKBgCjo
xpQ97VTDBWhFVjcxfLFqPIiO7X2MPFlLIa4zDBHq574hfwhJgLAXO8WBkLaGa1J8
7W52nSazT7HoDjTphPrFLYuyqUkbQyN3WLfCRn3fNOkeihnU25CjQMHVyYViYFi2
K1IRZXfTnq5bEoCysXQoiuO/hQw/OdcLK4hUmWzVAoGBAO2dAGdQ9tI5xVd6U9E2
2tpILCFJV55Ccge3VHUyxS+1w9R9Q6/zcj77tIAzAQRx50giAC3or/V3U+brPeIW
VnPBv4td6uQnHkLwYkDgxsxxP/14zsu9+qvTabc3i9z1bdI8pIn5XkDuqTyP5tbO
sXtpwZxmikzPR9KTlQMKJnZU
-----END RSA PRIVATE KEY-----
', '-----BEGIN RSA PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtKYGhjfXvBM05s9MWPs9
rmt5dPKt2JwTEFgsSRPpvCNwYh+SSBCjQegvVxfV9In9Ek4W298VtJjvBNUQF/zq
PLVaad7yQxR4O2YGJpwoQtRqkpSxbo/U00cCMKsBMUzHRt8VEbp2PN+oanQ9CnRf
56nLFfcf1ENT2gnw0ixKHrluqi7VzkoPRY7obiwO/Th4yLgeeCW3xnACll3SXH/D
9on9F+hRCcqkPEn0Cem4riS/wcQgYsBEpS2T5TmXDh1u+4v8Vc9xw84my0v01jko
xAWQ6KPoWsmymuOiH0/59oNnHCPkod7jewnc3I7lRrfiJmV7SgOq/wCEk0TAvMCc
+QIDAQAB
-----END RSA PUBLIC KEY-----
', now(), 'infinity');
INSERT INTO af_oauth.managed_key (id, algorithm, identity_id, key, public_key, activated_on, deactivated_on)
VALUES (2, 'EC', '2', '-----BEGIN EC PRIVATE KEY-----
MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCArWxjsos4qF2QtmsWW
oOkvzDFXTgMBqjDnZf1pn656WA==
-----END EC PRIVATE KEY-----
', '-----BEGIN EC PUBLIC KEY-----
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEXE7E/LWa0vQRiOctPsISFfpunu78
4kkB0xPQssFM6yH3XKQsFDQunA9dEWdBJRIwe0RxdopUnGBcebOufvGHWw==
-----END EC PUBLIC KEY-----
', now(), 'infinity');
alter sequence af_oauth.managed_key_id_seq restart 3;
