#Demo project for Spring Boot with Angular, csrf, cxf, oauth

Steps:
1. Init SpringBoot proj: done
2. Add rest controller: done
3. Create angular app https://github.com/opencodes/ng-14-app/tree/master: done
4. Add /dest to SpringBoot: done (manually)
5. Add call from Angular to SpringBoot: done
6. Add CXF: tried baeldung and cxf doc, bypass with mock api services: done
7. Add SpringBoot Security for angular resource: done
8. AngularServletFilter: lang chooser?
9. SecurityConfig.Cors: done
10. SecurityConfig.csrf: done
11. SecurityConfig.oauth2Login: done
12. Real CXF: done
13. Nginx for SPA serving

#Notes

#first time:
IJ set JDK17
IJ set node interpreter or 
CMDs:
./frontend/npm install
./frontend/npm run build (this could be automated, not in scope)
./frontend/npm run start
./target/java -jar sbng-0.0.1-SNAPSHOT.jar
#Keycloak users (realm "sbng", see keycloak/realm-export.json; password = username)
| user        | realm role      | can log in |
|-------------|-----------------|------------|
| q           | SBNG_ROLE_ADMIN | yes        |
| sbng-admin  | SBNG_ROLE_ADMIN | yes        |
| sbng-viewer | SBNG_ROLE_VIEW  | yes        |
| sbng-norole | (none)          | no - login is refused, ends on /login?error |

The realm is only imported when Keycloak starts without it, so after changing realm-export.json run `docker compose down` before `docker compose up`.
To switch user, end the Keycloak SSO session first: http://localhost:9080/realms/sbng/protocol/openid-connect/logout
