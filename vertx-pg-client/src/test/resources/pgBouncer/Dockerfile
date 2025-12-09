FROM fedora:43
RUN dnf -y update \
    && dnf -y install pgbouncer \
    && dnf clean all
RUN mkdir -p /var/run/pgbouncer /var/log/pgbouncer \
    && chown -R pgbouncer:pgbouncer /var/run/pgbouncer /var/log/pgbouncer /etc/pgbouncer
EXPOSE 6432
USER pgbouncer
CMD ["pgbouncer", "/etc/pgbouncer/pgbouncer.ini"]
