# CommandHider
[CommandHider]() 是一个 Minecraft 服务器(Spigot)的隐藏命令TAB补全插件。它工作在白名单模式下。
* 推荐和[CommandBlocker]()搭配使用。

## 特性

* 并非所有命令都可以通过让玩家没有使用该命令的权限来从TAB列表中隐藏。
* 配置中列出的命令将不会出现在TAB列表中，也可以让某些命令出现(就算没有这个命令)。
```diff
- 此插件只会关闭/开启某个命令的TAB补全提示，不会禁止玩家使用这个命令。
```

## 命令

`/commandhider reload` 重载配置文件

## 配置文件(config.yml)
```yaml
  # 填写all代表所有命令

  # 需要隐藏的命令
  hiddencommands:
    - all

  # 需要显示的命令
  showcommands:
    - exp1