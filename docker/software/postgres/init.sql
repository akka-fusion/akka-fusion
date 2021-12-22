set timezone to 'Asia/Chongqing';

create extension adminpack;

----------------------------------------
-- #functions
----------------------------------------

-- 将数组反序
create or replace function array_reverse(anyarray)
    returns anyarray as
$$
select array(
               select $1[i] from generate_subscripts($1, 1) as s (i) order by i desc
           );
$$
    language 'sql'
    strict
    immutable;
----------------------------------------
-- #functions
----------------------------------------

----------------------------------------
-- init tables, views, sequences  begin
----------------------------------------

----------------------------------------
-- init tables, views, sequences  end
----------------------------------------

-- change tables, views, sequences owner to massdata
-- DO
-- $$
--     DECLARE
--         r record;
--     BEGIN
--         FOR r IN SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'
--             LOOP
--                 EXECUTE 'alter table ' || r.table_name || ' owner to massdata;';
--             END LOOP;
--     END
-- $$;
-- DO
-- $$
--     DECLARE
--         r record;
--     BEGIN
--         FOR r IN select sequence_name from information_schema.sequences where sequence_schema = 'public'
--             LOOP
--                 EXECUTE 'alter sequence ' || r.sequence_name || ' owner to massdata;';
--             END LOOP;
--     END
-- $$;
-- DO
-- $$
--     DECLARE
--         r record;
--     BEGIN
--         FOR r IN select table_name from information_schema.views where table_schema = 'public'
--             LOOP
--                 EXECUTE 'alter table ' || r.table_name || ' owner to massdata;';
--             END LOOP;
--     END
-- $$;
-- grant all privileges on all tables in schema public to massdata;
-- grant all privileges on all sequences in schema public to massdata;

-- 批量 grant/ revoke 用户权限
create or replace function g_or_v(g_or_v text, -- 输入 grant or revoke 表示赋予或回收
                                  own name, -- 指定用户 owner
                                  target name, -- 赋予给哪个目标用户 grant privilege to who?
                                  objtyp text, --  对象类别: 表, 物化视图, 视图 object type 'r', 'v' or 'm', means table,view,materialized view
                                  exp text[], --  排除哪些对象, 用数组表示, excluded objects
                                  priv text --  权限列表, privileges, ,splits, like 'select,insert,update'
) returns void as
$$
declare
    nsp     name;
    rel     name;
    sql     text;
    tmp_nsp name := '';
begin
    for nsp,rel in select t2.nspname, t1.relname
                   from pg_class t1,
                        pg_namespace t2
                   where t1.relkind = objtyp
                     and t1.relnamespace = t2.oid
                     and t1.relowner = (select oid from pg_roles where rolname = own)
        loop
            if (tmp_nsp = '' or tmp_nsp <> nsp) and lower(g_or_v) = 'grant' then
                -- auto grant schema to target user
                sql := 'GRANT usage on schema "' || nsp || '" to ' || target;
                execute sql;
                raise notice '%', sql;
            end if;

            tmp_nsp := nsp;

            if (exp is not null and nsp || '.' || rel = any (exp)) then
                raise notice '% excluded % .', g_or_v, nsp || '.' || rel;
            else
                if lower(g_or_v) = 'grant' then
                    sql := g_or_v || ' ' || priv || ' on "' || nsp || '"."' || rel || '" to ' || target;
                elsif lower(g_or_v) = 'revoke' then
                    sql := g_or_v || ' ' || priv || ' on "' || nsp || '"."' || rel || '" from ' || target;
                else
                    raise notice 'you must enter grant or revoke';
                end if;
                raise notice '%', sql;
                execute sql;
            end if;
        end loop;
end;
$$ language plpgsql;
