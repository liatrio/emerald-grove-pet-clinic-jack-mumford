docker_compose('docker-compose.yml')

local_resource(
    'petclinic',
    cmd='true',
    serve_cmd='./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres',
    deps=[
        'pom.xml',
        'src/main/java',
        'src/main/resources',
    ],
    ignore=[
        '**/target/**',
    ],
    readiness_probe=probe(
        http_get=http_get_action(8080, path='/'),
        initial_delay_secs=15,
        period_secs=10,
        timeout_secs=2,
        failure_threshold=6,
    ),
    links=['http://localhost:8080'],
    resource_deps=['postgres'],
    trigger_mode=TRIGGER_MODE_AUTO,
)
