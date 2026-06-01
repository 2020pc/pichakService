insert into oauth_client_details(client_id, resource_ids, client_secret, scope, authorized_grant_types,
                                 web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity,
                                 additional_information, autoapprove)
values ('test', 'resource-server-rest-api', '$2a$04$J0uhcwKOKDIcHAByNomWIOSP61je2W0NxbUZo55SlKahe7Af5oK/a', 'read',
        'password,authorization_code,refresh_token,client_credentials,implicit',
        'https://localhost:8082/oauth2/callback', 'USER', 10800, 2592000, null, null);
-- insert ignore into authority(name) values ('ROLE_USER');

insert into users(account_expired, account_locked, credentials_expired, enabled, password, user_name)
values (0, 0, 0, 1, '$2a$04$0Y6WILG8jbx8eAsQflV7yO4XZBFA6ybLUeFAVyZXUYxaOHp9YeBWS', 'ATM_PARSIAN');
insert into user_authority(authority_id, user_id) values (3, select id from users where user_name = 'ATM_PARSIAN');

pasrian:'$2a$04$djXFr/vNT9U.b5A1No6XZuO3CbUnmdXSFU3N4U63DNo2JUgASMIJG'

java
-jar h2*.jar -webAllowOthers -tcpAllowOthers

insert into AUTHORITY (name) values('ROLE_USER')
insert into AUTHORITY (name) values('ROLE_ADMIN')
insert into AUTHORITY (name) values('ROLE_ATM')

nohup java -jar AAAServer-1.0.0.jar & --spring.config.location=application.properties

java -jar app.jar --spring.config.name=application,jdbc --spring.config.location=file:///Users/home/config
