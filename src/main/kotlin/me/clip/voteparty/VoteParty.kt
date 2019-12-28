package me.clip.voteparty

import co.aikar.commands.PaperCommandManager
import com.google.gson.Gson
import com.sxtanna.korm.Korm
import me.clip.voteparty.base.State
import me.clip.voteparty.cmds.CommandVoteParty
import me.clip.voteparty.conf.ConfigVoteParty
import me.clip.voteparty.handler.VoteHandler
import me.clip.voteparty.lang.Lang
import me.clip.voteparty.listener.VoteListener
import me.clip.voteparty.plugin.VotePartyPlugin
import me.clip.voteparty.plugin.XMaterial
import me.clip.voteparty.update.UpdateChecker
import java.util.*

class VoteParty internal constructor(private val plugin: VotePartyPlugin) : State
{

	private var conf = null as? ConfigVoteParty?
	private val cmds = PaperCommandManager(plugin)
	private val voteListener = VoteListener(plugin)
	val voteHandler = VoteHandler(plugin)


	override fun load()
	{
		val lang = Lang()
		lang.save(plugin.dataFolder)

		loadConfig()
		loadCommands()
		registerLanguages()

		UpdateChecker.check(plugin, 987)
		{

		}
		voteListener.load()
	}

	override fun kill()
	{
		voteListener.kill()
	}


	private fun loadConfig()
	{
		val file = plugin.dataFolder.resolve("conf.korm")

		val conf = (KORM.pull(file).to() ?: ConfigVoteParty.DEF).apply()
		{
			merge(ConfigVoteParty.DEF)
		}

		KORM.push(conf, file)

		this.conf = conf
	}

	private fun loadCommands()
	{
		cmds.enableUnstableAPI("help")

		cmds.registerCommand(CommandVoteParty())
	}

	fun conf() = conf ?: ConfigVoteParty.DEF

	private fun registerLanguages() {
		plugin.dataFolder.resolve("languages").listFiles()?.forEach {
			val tag = it.name.replace(".yml", "")
			val locale = Locale.forLanguageTag(tag)
			cmds.addSupportedLanguage(locale)
			cmds.locales.loadYamlLanguageFile(it, locale)
		}
		// Temp for now
		cmds.locales.defaultLocale = Locale.forLanguageTag(conf?.settings?.language ?: "en_US")
	}

	companion object
	{
		internal val GSON = Gson()
		internal val KORM = Korm()

		init {
		    KORM.pullWith<XMaterial> {reader, types ->
				types.firstOrNull()?.asBase()?.dataAsString()?.let(XMaterial::matchXMaterial)?.orElse(null)
			}
		}
	}


	// api methods

}