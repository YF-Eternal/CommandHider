# CommandHider
[CommandHider]() 是一个 Minecraft 服务器(Spigot)的隐藏命令TAB补全插件。它工作在白名单模式下。
* 由于一些原因 CommandHider 仅支持 Minecraft 1.8.x - 1.19.x 版本。
* 推荐和[CommandBlocker]()搭配使用。

## 特性

* 并非所有命令都可以通过让玩家没有使用该命令的权限来从TAB列表中隐藏。
* 配置中列出的命令将不会出现在TAB列表中，也可以让某些命令出现(就算没有这个命令)。
* 1.13+仅支持屏蔽一级命令

```diff
- 此插件只会关闭/开启某个命令的TAB补全提示，不会禁止玩家使用这个命令。
```

## 命令

`/commandhider reload` 重载配置文件

## 配置文件(config.yml)
```yaml
  # 需要隐藏的命令(填写all代表隐藏所有命令)
  hiddencommands:
    - examplecommand1
    - examplecommand2

  # 需要显示的命令
  showcommands:
    - examplecommand3
    - examplecommand4
